package com.toolrental.application.payment.usecase;

import com.toolrental.application.payment.dto.AvailabilityReportResponse;
import com.toolrental.application.payment.dto.FrequentCustomerReportResponse;
import com.toolrental.application.payment.dto.IncomeReportResponse;
import com.toolrental.application.payment.dto.ProfitabilityReportResponse;
import com.toolrental.application.payment.dto.ToolRentalReportResponse;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.payment.port.OperationalReportingPort;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class OperationalReportUseCase {
  private final OperationalReportingPort reports;
  private final AccessPolicy access;
  private final UnitOfWork work;
  private final Clock clock;

  public OperationalReportUseCase(
      OperationalReportingPort reports, AccessPolicy access, UnitOfWork work, Clock clock) {
    this.reports = reports;
    this.access = access;
    this.work = work;
    this.clock = clock;
  }

  public List<IncomeReportResponse> income(
      UUID actor, UUID requestedSupplierId, LocalDate from, LocalDate to) {
    validatePeriod(from, to);
    return work.execute(
        () ->
            reports.income(scope(actor, requestedSupplierId), from, to).stream()
                .map(
                    view ->
                        new IncomeReportResponse(
                            from,
                            to,
                            view.currency(),
                            view.grossIncome(),
                            view.refunds(),
                            view.netIncome(),
                            view.confirmedPayments()))
                .toList());
  }

  public List<ToolRentalReportResponse> mostRentedTools(
      UUID actor, UUID requestedSupplierId, LocalDate from, LocalDate to) {
    validatePeriod(from, to);
    return work.execute(
        () ->
            reports.mostRentedTools(scope(actor, requestedSupplierId), from, to).stream()
                .map(
                    view ->
                        new ToolRentalReportResponse(
                            view.toolId(),
                            view.toolName(),
                            view.currency(),
                            view.reservationCount(),
                            view.rentedDays(),
                            view.bookedRevenue()))
                .toList());
  }

  public List<FrequentCustomerReportResponse> frequentCustomers(
      UUID actor, UUID requestedSupplierId, LocalDate from, LocalDate to) {
    validatePeriod(from, to);
    return work.execute(
        () ->
            reports.frequentCustomers(scope(actor, requestedSupplierId), from, to).stream()
                .map(
                    view ->
                        new FrequentCustomerReportResponse(
                            view.customerId(),
                            view.customerName(),
                            view.currency(),
                            view.reservationCount(),
                            view.netIncome()))
                .toList());
  }

  public List<AvailabilityReportResponse> availability(
      UUID actor, UUID requestedSupplierId, LocalDate from, LocalDate to) {
    validatePeriod(from, to);
    return work.execute(
        () ->
            reports
                .availability(scope(actor, requestedSupplierId), from, to, Instant.now(clock))
                .stream()
                .map(
                    view ->
                        new AvailabilityReportResponse(
                            from,
                            to,
                            view.toolId(),
                            view.toolName(),
                            view.totalUnits(),
                            view.availableUnits(),
                            view.reservedUnits(),
                            view.maintenanceUnits(),
                            view.retiredUnits()))
                .toList());
  }

  public List<ProfitabilityReportResponse> profitability(
      UUID actor, UUID requestedSupplierId, LocalDate from, LocalDate to) {
    validatePeriod(from, to);
    return work.execute(
        () ->
            reports.profitability(scope(actor, requestedSupplierId), from, to).stream()
                .map(
                    view ->
                        new ProfitabilityReportResponse(
                            view.toolId(),
                            view.toolName(),
                            view.currency(),
                            view.reservationCount(),
                            view.rentedDays(),
                            view.bookedRevenue(),
                            view.averageRevenuePerReservation()))
                .toList());
  }

  private UUID scope(UUID actor, UUID requestedSupplierId) {
    access.requireActive(actor);
    if (access.isAdministrator(actor)) return requestedSupplierId;
    access.requireRole(actor, "SUPPLIER");
    if (requestedSupplierId != null) access.requireOwnerOrAdministrator(actor, requestedSupplierId);
    return actor;
  }

  private static void validatePeriod(LocalDate from, LocalDate to) {
    if (from == null || to == null || from.isAfter(to))
      throw new BusinessRuleException("Report period is invalid");
  }
}
