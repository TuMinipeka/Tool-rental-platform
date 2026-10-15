package com.toolrental.infrastructure.category.adapters.in.rest.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
    @NotBlank @Size(max = 120) String name, @Size(max = 500) String description) {}
