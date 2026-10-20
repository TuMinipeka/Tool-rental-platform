package com.toolrental.application.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FrequentCustomerReportResponse(
    UUID customerId,
    String customerName,
    String currency,
    long reservationCount,
    BigDecimal netIncome) {}
