package com.toolrental.application.damagereport.usecase;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.damagereport.command.RegisterDamageReportCommand;
import com.toolrental.application.damagereport.dto.DamageReportResponse;
import com.toolrental.application.damagereport.exception.DamageReportNotFoundApplicationException;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.damagereport.model.aggregate.DamageReport;
import com.toolrental.domain.damagereport.model.valueobject.DamageReportId;
import com.toolrental.domain.damagereport.port.repository.DamageReportRepository;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import com.toolrental.domain.unitreturn.model.valueobject.UnitReturnId;
import com.toolrental.domain.unitreturn.port.repository.UnitReturnRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ManageDamageReportUseCase {
  private final DamageReportRepository repository;
  private final UnitOfWork work;
  private final AccessPolicy access;
  private final WorkflowReferences references;
  private final Clock clock;
  private final UnitReturnRepository returns;

  public ManageDamageReportUseCase(
      DamageReportRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      UnitReturnRepository returns) {
    this.repository = repository;
    this.work = work;
    this.access = access;
    this.references = references;
    this.clock = clock;
    this.returns = returns;
  }

  private DamageReport load(DamageReportId id) {
    return repository.findById(id).orElseThrow(DamageReportNotFoundApplicationException::new);
  }

  public DamageReportResponse find(UUID actor, DamageReportId id) {
    return work.execute(
        () -> {
          var value = load(id);
          references.reservation(actor, value.reservationId(), false);
          return DamageReportResponse.from(value);
        });
  }

  public List<DamageReportResponse> list(UUID actor) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          return repository.findAll().stream().map(DamageReportResponse::from).toList();
        });
  }

  public DamageReportResponse register(UUID actor, RegisterDamageReportCommand command) {
    return work.execute(
        () -> {
          var reservation = references.reservation(actor, command.reservationId(), true);
          WorkflowReferences.require(
              reservation.status() == ReservationStatus.CONFIRMED, "Reservation must be confirmed");
          references.item(reservation, command.equipmentUnitId());
          WorkflowReferences.reference(
              returns
                  .findById(new UnitReturnId(command.reservationId(), command.equipmentUnitId()))
                  .isPresent(),
              "Return");
          WorkflowReferences.require(
              command.assessedCost() == null || command.assessedCost().signum() == 0,
              "Assess cost through the assessment operation");
          var id = DamageReportId.generate();
          if (repository.findById(id).isPresent())
            throw new BusinessRuleException("DamageReport already exists");
          var value =
              DamageReport.register(
                  id,
                  command.reservationId(),
                  command.equipmentUnitId(),
                  Instant.now(clock),
                  command.description(),
                  "OPEN",
                  null,
                  clock);
          var saved = repository.save(value);
          return DamageReportResponse.from(saved);
        });
  }

  public DamageReportResponse assess(UUID actor, DamageReportId id, BigDecimal cost) {
    return work.execute(
        () -> {
          var value = load(id);
          references.reservation(actor, value.reservationId(), true);
          return DamageReportResponse.from(repository.save(value.assess(cost, clock)));
        });
  }

  public DamageReportResponse transition(UUID actor, DamageReportId id, String next) {
    return work.execute(
        () -> {
          var value = load(id);
          references.reservation(actor, value.reservationId(), true);
          return DamageReportResponse.from(repository.save(value.transition(next, clock)));
        });
  }
}
