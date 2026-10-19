package com.toolrental.application.user.dto;

import java.time.Instant;

public record PasswordRecoveryResponse(boolean accepted, String recoveryToken, Instant expiresAt) {}
