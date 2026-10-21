package com.toolrental.application.payment.command;

public record ProcessSandboxWebhookCommand(
    String eventId, String externalReference, String outcome) {}
