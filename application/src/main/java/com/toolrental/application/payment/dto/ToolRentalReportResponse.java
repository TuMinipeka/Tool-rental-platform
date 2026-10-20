package com.toolrental.application.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ToolRentalReportResponse(
    UUID toolId,
    String toolName,
    String currency,
    long reservationCount,
    long rentedDays,
    BigDecimal bookedRevenue) {}
