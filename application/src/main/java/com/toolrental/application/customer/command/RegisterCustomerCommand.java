package com.toolrental.application.customer.command;

import com.toolrental.domain.user.model.valueobject.UserId;

public record RegisterCustomerCommand(
    UserId userId,
    String personType,
    String documentType,
    String documentNumber,
    String billingName) {}
