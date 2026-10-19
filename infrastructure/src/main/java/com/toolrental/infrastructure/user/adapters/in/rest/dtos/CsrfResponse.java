package com.toolrental.infrastructure.user.adapters.in.rest.dtos;

public record CsrfResponse(String headerName, String parameterName, String token) {}
