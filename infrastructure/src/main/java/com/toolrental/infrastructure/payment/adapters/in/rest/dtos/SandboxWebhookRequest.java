package com.toolrental.infrastructure.payment.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotBlank;

public record SandboxWebhookRequest(
    @NotBlank String eventId, @NotBlank String externalReference, @NotBlank String outcome) {}
