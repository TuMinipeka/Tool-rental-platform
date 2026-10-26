package com.toolrental.application.payment.dto;

import java.time.LocalDate;
import java.util.UUID;

public record OverdueRentalReportResponse(
    UUID reservationId,
    UUID customerId,
    String customerName,
    LocalDate expectedReturnDate,
    long overdueDays,
    String rentalStatus,
    long unitCount) {}
