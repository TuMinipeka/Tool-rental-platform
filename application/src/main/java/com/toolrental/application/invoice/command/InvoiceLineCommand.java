package com.toolrental.application.invoice.command;

import java.math.BigDecimal;

public record InvoiceLineCommand(String description, BigDecimal quantity, BigDecimal unitPrice) {}
