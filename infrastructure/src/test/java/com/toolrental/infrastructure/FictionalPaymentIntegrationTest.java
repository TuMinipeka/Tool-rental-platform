package com.toolrental.infrastructure;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toolrental.application.payment.usecase.CreateHostedCheckoutUseCase;
import com.toolrental.application.payment.usecase.DecideFictionalCheckoutUseCase;
import com.toolrental.application.payment.usecase.ReconcileHostedPaymentUseCase;
import com.toolrental.application.reservation.command.RequestReservationCommand;
import com.toolrental.application.reservation.usecase.RequestReservationUseCase;
import com.toolrental.domain.customer.model.valueobject.CustomerId;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.payment.port.FictionalPaymentDecision;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
    properties = {
      "payments.provider=fictional",
      "payments.internal-enabled=false",
      "auth.password-recovery.enabled=false",
      "payments.reconciliation-delay-ms=3600000"
    })
@AutoConfigureMockMvc
@Testcontainers
class FictionalPaymentIntegrationTest {
  @Container
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

  @DynamicPropertySource
  static void database(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired JdbcTemplate db;
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired RequestReservationUseCase request;
  @Autowired CreateHostedCheckoutUseCase create;
  @Autowired DecideFictionalCheckoutUseCase decide;
  @Autowired ReconcileHostedPaymentUseCase reconcile;
  @Autowired FictionalPaymentDecision decisions;
  UUID customer, supplier, other, unit;

  @BeforeEach
  void seed() {
    db.execute(
        "truncate invoice_items,invoices,payments,damage_reports,unit_returns,unit_deliveries,rentals,reservation_items,reservations,equipment_units,tool_images,tools,categories,customers,suppliers,user_roles,users cascade");
    customer = UUID.randomUUID();
    supplier = UUID.randomUUID();
    other = UUID.randomUUID();
    unit = UUID.randomUUID();
    for (var id : List.of(customer, supplier, other))
      db.update(
          "insert into users values (?,?,?,'test-hash','ACTIVE',now())",
          id,
          id + "@example.test",
          "Fictional Test");
    db.update("insert into user_roles select ?,role_id from roles where code='CUSTOMER'", customer);
    db.update("insert into user_roles select ?,role_id from roles where code='CUSTOMER'", other);
    db.update("insert into user_roles select ?,role_id from roles where code='SUPPLIER'", supplier);
    db.update(
        "insert into customers values (?,'PERSON','ID','FC-1','Fictional Customer')", customer);
    db.update("insert into customers values (?,'PERSON','ID','FC-2','Other Customer')", other);
    db.update("insert into suppliers values (?,'ID','FS-1','Fictional Supplier')", supplier);
    var category = UUID.randomUUID();
    var tool = UUID.randomUUID();
    db.update("insert into categories values (?,'Fictional tools','Test category')", category);
    db.update(
        "insert into tools values (?,?,?,'Drill','Test drill',25000,'COP','PUBLISHED')",
        tool,
        supplier,
        category);
    db.update(
        "insert into equipment_units values (?,?,'F-INV','F-SERIAL','GOOD','OPERATIONAL')",
        unit,
        tool);
  }

  com.toolrental.application.reservation.dto.ReservationResponse held() {
    var date = LocalDate.now(ZoneId.of("America/Bogota")).plusDays(1);
    return request.execute(
        new RequestReservationCommand(
            new CustomerId(customer),
            new ReservationPeriod(date, date),
            List.of(new EquipmentUnitId(unit))));
  }

  @Test
  void fictionalApprovalUsesPersistedAmountAndIssuesOnlyOneInvoice() throws Exception {
    var reservation = held();
    var checkout = create.execute(customer, reservation.id(), "fictional-approve");
    assertThat(checkout.processor()).isEqualTo("FICTIONAL_GATEWAY");
    assertThat(checkout.checkoutUrl()).isEqualTo("/checkout/" + checkout.paymentId());
    assertThat(checkout.amount()).isEqualByComparingTo("25000");
    for (int n = 0; n < 2; n++)
      mvc.perform(
              post("/api/v1/payments/fictional/checkouts/" + checkout.paymentId() + "/decision")
                  .with(jwt().jwt(t -> t.subject(customer.toString())))
                  .contentType("application/json")
                  .content(json.writeValueAsString(Map.of("outcome", "APPROVED"))))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.paymentStatus").value("DONE"));
    assertThat(db.queryForObject("select count(*) from invoices", Integer.class)).isEqualTo(1);
    assertThat(
            db.queryForObject(
                "select reservation_status from reservations where reservation_id=?",
                String.class,
                reservation.id().value()))
        .isEqualTo("CONFIRMED");
    mvc.perform(get("/api/v1/public/features"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paymentProvider").value("FICTIONAL_GATEWAY"))
        .andExpect(jsonPath("$.hostedCheckout").value(true));
  }

  @Test
  void fictionalDecisionRequiresOwnerAndRejectsContradictoryOrInvalidOutcomes() throws Exception {
    var checkout = create.execute(customer, held().id(), "fictional-owner");
    var path = "/api/v1/payments/fictional/checkouts/" + checkout.paymentId() + "/decision";
    mvc.perform(post(path).contentType("application/json").content("{}"))
        .andExpect(status().isUnauthorized());
    mvc.perform(
            post(path)
                .with(jwt().jwt(t -> t.subject(other.toString())))
                .contentType("application/json")
                .content(json.writeValueAsString(Map.of("outcome", "APPROVED"))))
        .andExpect(status().isForbidden());
    mvc.perform(
            post(path)
                .with(jwt().jwt(t -> t.subject(supplier.toString())))
                .contentType("application/json")
                .content(json.writeValueAsString(Map.of("outcome", "APPROVED"))))
        .andExpect(status().isForbidden());
    mvc.perform(
            post(path)
                .with(jwt().jwt(t -> t.subject(customer.toString())))
                .contentType("application/json")
                .content(json.writeValueAsString(Map.of("outcome", "INVALID"))))
        .andExpect(status().isBadRequest());
    decide.execute(customer, checkout.paymentId(), "APPROVED");
    assertThatThrownBy(() -> decide.execute(customer, checkout.paymentId(), "DECLINED"))
        .isInstanceOf(
            com.toolrental.application.common.exception.IdempotencyConflictApplicationException
                .class);
  }

  @Test
  void rejectionReleasesUnitAndExpiredCheckoutCannotBeApproved() {
    var checkout = create.execute(customer, held().id(), "fictional-decline");
    assertThat(decide.execute(customer, checkout.paymentId(), "DECLINED").paymentStatus())
        .isEqualTo("REJECTED");
    var next = held();
    var expired = create.execute(customer, next.id(), "fictional-expired");
    db.update(
        "update reservations set expires_at=now()-interval '1 minute' where reservation_id=?",
        next.id().value());
    assertThatThrownBy(() -> decide.execute(customer, expired.paymentId(), "APPROVED"))
        .isInstanceOf(com.toolrental.domain.common.exception.BusinessRuleException.class);
    assertThat(db.queryForObject("select count(*) from invoices", Integer.class)).isZero();
  }

  @Test
  void persistedFictionalApprovalSurvivesInterruptedProcessingAndRefundsIfLate() {
    var reservation = held();
    var checkout = create.execute(customer, reservation.id(), "fictional-recovery");
    decisions.decide(checkout.paymentId(), "APPROVED");
    db.update(
        "update reservations set expires_at=now()-interval '1 minute' where reservation_id=?",
        reservation.id().value());
    reconcile.execute(checkout.paymentId());
    reconcile.execute(checkout.paymentId());
    assertThat(
            db.queryForObject(
                "select payment_status from payments where payment_id=?",
                String.class,
                checkout.paymentId()))
        .isEqualTo("REFUNDED");
    assertThat(
            db.queryForObject(
                "select refund_status from refunds where payment_id=?",
                String.class,
                checkout.paymentId()))
        .isEqualTo("CONFIRMED");
    assertThat(db.queryForObject("select count(*) from invoices", Integer.class)).isZero();
  }

  @Test
  void simultaneousFictionalApprovalsNeverDuplicatePaymentOrInvoice() throws Exception {
    var checkout = create.execute(customer, held().id(), "fictional-concurrent");
    try (var pool = Executors.newFixedThreadPool(2)) {
      var barrier = new CyclicBarrier(2);
      var jobs = new java.util.ArrayList<java.util.concurrent.Future<?>>();
      for (int n = 0; n < 2; n++)
        jobs.add(
            pool.submit(
                () -> {
                  barrier.await();
                  try {
                    decide.execute(customer, checkout.paymentId(), "APPROVED");
                  } catch (com.toolrental.domain.common.exception.BusinessRuleException busy) {
                    assertThat(busy.getMessage()).contains("in progress");
                  }
                  return null;
                }));
      for (var job : jobs) job.get(20, TimeUnit.SECONDS);
    }
    reconcile.execute(checkout.paymentId());
    assertThat(db.queryForObject("select count(*) from invoices", Integer.class)).isEqualTo(1);
    assertThat(db.queryForObject("select count(*) from payments", Integer.class)).isEqualTo(1);
  }
}
