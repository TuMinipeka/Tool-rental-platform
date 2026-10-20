package com.toolrental.application.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeReportResponse(
    LocalDate from,
    LocalDate to,
    String currency,
    BigDecimal grossIncome,
    BigDecimal refunds,
    BigDecimal netIncome,
    long confirmedPayments) {}
