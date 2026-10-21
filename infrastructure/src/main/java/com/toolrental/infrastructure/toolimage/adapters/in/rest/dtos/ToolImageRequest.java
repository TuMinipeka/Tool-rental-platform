package com.toolrental.infrastructure.toolimage.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ToolImageRequest(@NotNull UUID toolId, @NotBlank String imageUrl) {}
