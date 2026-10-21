package com.toolrental.application.reservationextension.usecase;

import com.toolrental.application.common.exception.AvailabilityConflictApplicationException;
import com.toolrental.application.common.exception.IdempotencyConflictApplicationException;
import com.toolrental.application.common.exception.ReferenceNotFoundApplicationException;
import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.reservationextension.dto.ReservationExtensionResponse;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.payment.model.aggregate.Payment;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import com.toolrental.domain.payment.port.repository.PaymentRepository;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import com.toolrental.domain.reservation.model.valueobject.ReservationStatus;
import com.toolrental.domain.reservation.port.repository.AtomicReservationManagement;
import com.toolrental.domain.reservationextension.model.aggregate.ReservationExtension;
import com.toolrental.domain.reservationextension.model.valueobject.ReservationExtensionId;
import com.toolrental.domain.reservationextension.port.repository.ReservationExtensionRepository;
import com.toolrental.domain.user.model.valueobject.UserId;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public final class ManageReservationExtensionUseCase {
  private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Bogota");
  private final ReservationExtensionRepository extensions;
  private final AtomicReservationManagement reservations;
  private final PaymentRepository payments;
  private final WorkflowReferences references;
  private final AccessPolicy access;
  private final UnitOfWork work;
  private final Clock clock;

  public ManageReservationExtensionUseCase(
      ReservationExtensionRepository extensions,
      AtomicReservationManagement reservations,
      PaymentRepository payments,
      WorkflowReferences references,
      AccessPolicy access,
      UnitOfWork work,
      Clock clock) {
    this.extensions = extensions;
    this.reservations = reservations;
    this.payments = payments;
    this.references = references;
    this.access = access;
    this.work = work;
    this.clock = clock;
  }

  public ReservationExtensionResponse request(
      UUID actor, ReservationId reservationId, LocalDate requestedReturnDate, String key) {
    return work.execute(
        () -> {
          var repeated = extensions.findByIdempotencyKey(key);
          if (repeated.isPresent()) {
            if (!repeated.get().reservationId().equals(reservationId)
                || !repeated.get().requestedReturnDate().equals(requestedReturnDate))
              throw new IdempotencyConflictApplicationException();
            var original = references.reservation(actor, reservationId, false);
            access.requireOwnerOrAdministrator(actor, original.customerId().value());
            return ReservationExtensionResponse.from(repeated.get());
          }
          var reservation = references.reservation(actor, reservationId, false);
          access.requireOwnerOrAdministrator(actor, reservation.customerId().value());
          if (reservation.status() != ReservationStatus.CONFIRMED)
            throw new BusinessRuleException("Only confirmed reservations can be extended");
          var today = LocalDate.now(clock.withZone(BUSINESS_ZONE));
          if (today.isAfter(reservation.effectiveReturnDate()))
            throw new BusinessRuleException("Extension must be requested before the due date");
          if (!requestedReturnDate.isAfter(reservation.effectiveReturnDate()))
            throw new BusinessRuleException("Extension must increase the return date");
          if (extensions.findOpenByReservation(reservationId).isPresent())
            throw new BusinessRuleException("Reservation already has a pending extension");
          var extraDays =
              ChronoUnit.DAYS.between(reservation.effectiveReturnDate(), requestedReturnDate);
          var amount =
              reservation.items().stream()
                  .map(item -> item.dailyRate().multiply(extraDays))
                  .reduce(new Money(BigDecimal.ZERO, reservation.total().currency()), Money::add);
          var extension =
              ReservationExtension.request(
                  ReservationExtensionId.generate(),
                  reservationId,
                  new UserId(actor),
                  requestedReturnDate,
                  amount,
                  key,
                  Instant.now(clock));
          return ReservationExtensionResponse.from(extensions.save(extension));
        });
  }

  public ReservationExtensionResponse resolve(
      UUID actor, ReservationExtensionId extensionId, boolean approve) {
    return work.execute(
        () -> {
          var extension =
              extensions
                  .findById(extensionId)
                  .orElseThrow(
                      () -> new ReferenceNotFoundApplicationException("Reservation extension"));
          var reservation = references.reservation(actor, extension.reservationId(), true);
          if (approve) {
            var start = reservation.effectiveReturnDate().plusDays(1);
            var extensionPeriod = new ReservationPeriod(start, extension.requestedReturnDate());
            reservations.execute(
                reservation.id(),
                context -> {
                  for (var item : context.reservation().items())
                    if (context.isOccupied(item.equipmentUnitId(), extensionPeriod))
                      throw new AvailabilityConflictApplicationException();
                  context.reservation().extendTo(extension.requestedReturnDate());
                  return context.reservation();
                });
          }
          var resolved =
              extensions.save(
                  extension.resolve(
                      approve ? "APPROVED" : "REJECTED", new UserId(actor), Instant.now(clock)));
          if (approve && resolved.additionalAmount().amount().signum() > 0)
            payments.save(
                Payment.adjustment(
                    PaymentId.generate(),
                    reservation.id(),
                    resolved.additionalAmount().amount(),
                    "EXTENSION",
                    resolved.id().value(),
                    resolved.idempotencyKey() + ":adjustment",
                    clock));
          return ReservationExtensionResponse.from(resolved);
        });
  }

  public List<ReservationExtensionResponse> list(UUID actor, ReservationId reservationId) {
    return work.execute(
        () -> {
          references.reservation(actor, reservationId, false);
          return extensions.findByReservation(reservationId).stream()
              .map(ReservationExtensionResponse::from)
              .toList();
        });
  }
}
