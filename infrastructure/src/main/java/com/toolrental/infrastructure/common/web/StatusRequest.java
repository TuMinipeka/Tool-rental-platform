package com.toolrental.infrastructure.common.web;

import jakarta.validation.constraints.NotBlank;

public record StatusRequest(@NotBlank String status) {}
