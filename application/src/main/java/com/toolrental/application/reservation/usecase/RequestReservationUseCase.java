package com.toolrental.application.reservation.usecase;

import com.toolrental.application.equipmentunit.exception.EquipmentUnitNotFoundApplicationException;
import com.toolrental.application.reservation.command.RequestReservationCommand;
import com.toolrental.application.reservation.dto.ReservationResponse;
import com.toolrental.application.reservation.exception.CustomerNotFoundApplicationException;
import com.toolrental.application.supplier.exception.SupplierNotFoundApplicationException;
import com.toolrental.application.tool.exception.ToolNotFoundApplicationException;
import com.toolrental.domain.cancellationpolicy.model.valueobject.CancellationPolicyId;
import com.toolrental.domain.cancellationpolicy.port.repository.CancellationPolicyRepository;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.common.port.ParticipantsPort;
import com.toolrental.domain.deliveryrate.port.repository.DeliveryRateRepository;
import com.toolrental.domain.equipmentunit.port.repository.EquipmentUnitRepository;
import com.toolrental.domain.reservation.model.aggregate.Reservation;
import com.toolrental.domain.reservation.model.valueobject.FulfillmentMethod;
import com.toolrental.domain.reservation.port.repository.ReservationRepository;
import com.toolrental.domain.reservationitem.model.entity.ReservationLine;
import com.toolrental.domain.tool.port.repository.ToolRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

public final class RequestReservationUseCase {
  private final ReservationRepository reservations;
  private final EquipmentUnitRepository equipmentUnits;
  private final ToolRepository tools;
  private final ParticipantsPort participants;
  private final Clock clock;
  private final CancellationPolicyRepository policies;
  private final DeliveryRateRepository deliveryRates;

  public RequestReservationUseCase(
      ReservationRepository r,
      EquipmentUnitRepository u,
      ToolRepository h,
      ParticipantsPort p,
      Clock clock) {
    reservations = Objects.requireNonNull(r);
    equipmentUnits = Objects.requireNonNull(u);
    tools = Objects.requireNonNull(h);
    participants = Objects.requireNonNull(p);
    this.clock = Objects.requireNonNull(clock);
    policies = null;
    deliveryRates = null;
  }

  public RequestReservationUseCase(
      ReservationRepository r,
      EquipmentUnitRepository u,
      ToolRepository h,
      ParticipantsPort p,
      CancellationPolicyRepository policies,
      DeliveryRateRepository deliveryRates,
      Clock clock) {
    reservations = Objects.requireNonNull(r);
    equipmentUnits = Objects.requireNonNull(u);
    tools = Objects.requireNonNull(h);
    participants = Objects.requireNonNull(p);
    this.policies = Objects.requireNonNull(policies);
    this.deliveryRates = Objects.requireNonNull(deliveryRates);
    this.clock = Objects.requireNonNull(clock);
  }

  public ReservationResponse execute(RequestReservationCommand c) {
    if (!participants.isCustomerActive(c.customerId()))
      throw new CustomerNotFoundApplicationException(c.customerId());
    var items =
        c.equipmentUnits().stream()
            .map(
                id -> {
                  var u =
                      equipmentUnits
                          .findById(id)
                          .orElseThrow(() -> new EquipmentUnitNotFoundApplicationException(id));
                  var h =
                      tools
                          .findById(u.toolId())
                          .orElseThrow(() -> new ToolNotFoundApplicationException(u.toolId()));
                  if (!u.isOperational() || !h.published())
                    throw new BusinessRuleException("Equipment unit is not rentable");
                  if (!participants.isSupplierActive(h.supplierId()))
                    throw new SupplierNotFoundApplicationException(h.supplierId());
                  return new ReservationLine(id, h.supplierId(), h.dailyRate());
                })
            .toList();
    var policyId =
        policies == null
            ? new CancellationPolicyId(UUID.fromString("00000000-0000-0000-0000-000000000203"))
            : policies
                .findActive()
                .orElseThrow(() -> new BusinessRuleException("No active cancellation policy"))
                .id();
    var deliveryRate =
        c.fulfillmentMethod() == FulfillmentMethod.SUPPLIER_DELIVERY
            ? java.util.Optional.of(
                Objects.requireNonNull(deliveryRates)
                    .findActive()
                    .orElseThrow(() -> new BusinessRuleException("No active delivery rate")))
            : java.util.Optional
                .<com.toolrental.domain.deliveryrate.model.aggregate.DeliveryRate>empty();
    var deliveryFee =
        deliveryRate
            .map(com.toolrental.domain.deliveryrate.model.aggregate.DeliveryRate::price)
            .orElse(new Money(BigDecimal.ZERO, Currency.getInstance("COP")));
    var reservation =
        Reservation.request(
            c.customerId(),
            c.period(),
            items,
            c.scheduledFulfillmentAt(),
            c.fulfillmentMethod(),
            c.deliveryAddress(),
            deliveryRate.map(rate -> rate.id()).orElse(null),
            policyId,
            deliveryFee,
            clock);
    reservations.addRequest(reservation);
    return ReservationResponse.from(reservation);
  }
}
