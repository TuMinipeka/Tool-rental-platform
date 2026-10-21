package com.toolrental.infrastructure.invoice.adapters.in.rest.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record InvoiceRequest(
    @NotNull UUID reservationId,
    @NotBlank String number,
    @NotNull @Valid @NotEmpty List<InvoiceLineRequest> lines) {}
