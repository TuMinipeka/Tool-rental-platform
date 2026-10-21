package com.toolrental.infrastructure.tool.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotNull;

public record ToolPublicationRequest(@NotNull Boolean published) {}
