package com.toolrental.application.user.command;

public record RegisterAccountCommand(
    String email,
    String displayName,
    String password,
    String personType,
    String documentType,
    String documentNumber,
    String billingName) {}
