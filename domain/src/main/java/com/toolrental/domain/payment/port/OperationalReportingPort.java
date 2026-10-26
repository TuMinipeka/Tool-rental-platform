package com.toolrental.domain.payment.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface OperationalReportingPort {
  record IncomeView(
      String currency,
      BigDecimal grossIncome,
      BigDecimal refunds,
      BigDecimal netIncome,
      long confirmedPayments) {}

  record ToolRentalView(
      UUID toolId,
      String toolName,
      String currency,
      long reservationCount,
      long rentedDays,
      BigDecimal bookedRevenue) {}

  record FrequentCustomerView(
      UUID customerId,
      String customerName,
      String currency,
      long reservationCount,
      BigDecimal netIncome) {}

  record AvailabilityView(
      UUID toolId,
      String toolName,
      long totalUnits,
      long availableUnits,
      long reservedUnits,
      long maintenanceUnits,
      long retiredUnits) {}

  record ProfitabilityView(
      UUID toolId,
      String toolName,
      String currency,
      long reservationCount,
      long rentedDays,
      BigDecimal bookedRevenue,
      BigDecimal averageRevenuePerReservation) {}

  record OverdueRentalView(
      UUID reservationId,
      UUID customerId,
      String customerName,
      LocalDate expectedReturnDate,
      long overdueDays,
      String rentalStatus,
      long unitCount) {}

  List<IncomeView> income(UUID supplierId, LocalDate from, LocalDate to);

  List<ToolRentalView> mostRentedTools(UUID supplierId, LocalDate from, LocalDate to);

  List<FrequentCustomerView> frequentCustomers(UUID supplierId, LocalDate from, LocalDate to);

  List<AvailabilityView> availability(
      UUID supplierId, LocalDate from, LocalDate to, Instant currentTime);

  List<ProfitabilityView> profitability(UUID supplierId, LocalDate from, LocalDate to);

  List<OverdueRentalView> overdueRentals(UUID supplierId, LocalDate asOf);
}
