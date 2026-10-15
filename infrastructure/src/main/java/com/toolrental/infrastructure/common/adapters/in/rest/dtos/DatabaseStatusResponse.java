package com.toolrental.infrastructure.common.adapters.in.rest.dtos;

import java.time.Instant;

public record DatabaseStatusResponse(boolean connected, Instant checkedAt) {}
