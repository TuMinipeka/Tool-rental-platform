package com.toolrental.infrastructure.usersession.adapters.in.rest.dtos;

public record CsrfResponse(String headerName, String parameterName, String token) {}
