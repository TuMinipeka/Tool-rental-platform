package com.toolrental.application.supplier.command;

import com.toolrental.domain.user.model.valueobject.UserId;

public record RegisterSupplierCommand(
    UserId userId, String documentType, String documentNumber, String businessName) {}
