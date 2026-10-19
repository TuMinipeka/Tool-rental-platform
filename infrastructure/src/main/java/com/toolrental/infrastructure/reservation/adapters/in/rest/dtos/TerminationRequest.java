package com.toolrental.infrastructure.reservation.adapters.in.rest.dtos;

import jakarta.validation.constraints.Size;

public record TerminationRequest(@Size(max = 500) String reason) {}
