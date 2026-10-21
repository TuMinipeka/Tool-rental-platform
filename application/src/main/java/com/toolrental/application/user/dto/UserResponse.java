package com.toolrental.application.user.dto;

import com.toolrental.domain.user.model.aggregate.User;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID userId, String email, String displayName, String accountStatus, Instant registeredAt) {
  public static UserResponse from(User value) {
    return new UserResponse(
        value.id().value(),
        value.email(),
        value.displayName(),
        value.accountStatus(),
        value.registeredAt());
  }
}
