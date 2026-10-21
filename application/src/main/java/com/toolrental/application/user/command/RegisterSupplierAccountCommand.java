package com.toolrental.application.user.command;

public record RegisterSupplierAccountCommand(
    String email,
    String displayName,
    String password,
    String documentType,
    String documentNumber,
    String businessName) {}
