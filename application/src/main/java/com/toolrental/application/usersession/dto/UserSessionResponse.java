package com.toolrental.application.usersession.dto;

import com.toolrental.domain.usersession.model.aggregate.UserSession;
import java.time.Instant;
import java.util.UUID;

public record UserSessionResponse(
    UUID sessionId,
    String status,
    Instant createdAt,
    Instant expiresAt,
    String clientLabel,
    boolean current) {
  public static UserSessionResponse from(UserSession session, UUID currentSessionId) {
    return new UserSessionResponse(
        session.id().value(),
        session.status(),
        session.createdAt(),
        session.expiresAt(),
        session.clientLabel(),
        session.id().value().equals(currentSessionId));
  }
}
