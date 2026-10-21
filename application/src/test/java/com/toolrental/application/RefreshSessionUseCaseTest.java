package com.toolrental.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.toolrental.application.usersession.exception.RefreshTokenReuseApplicationException;
import com.toolrental.application.usersession.usecase.RefreshSessionUseCase;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.user.model.valueobject.SecretToken;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.domain.user.port.SecureTokenService;
import com.toolrental.domain.usersession.model.aggregate.UserSession;
import com.toolrental.domain.usersession.model.valueobject.UserSessionId;
import com.toolrental.domain.usersession.port.repository.UserSessionRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class RefreshSessionUseCaseTest {
  @Test
  void reusedRefreshRevokesFamilyInASeparateTransaction() {
    var now = Instant.parse("2026-01-01T00:00:30Z");
    var original =
        UserSession.start(
            UserSessionId.generate(),
            UUID.randomUUID(),
            new UserId(UUID.randomUUID()),
            "a".repeat(64),
            now.minusSeconds(30),
            now.plus(Duration.ofDays(30)),
            null);
    var rotated =
        original.rotate(
            UserSessionId.generate(),
            "b".repeat(64),
            now.minusSeconds(20),
            now.plus(Duration.ofDays(30)));
    var repository = new SessionRepository(rotated.rotated());
    var work = new CountingUnitOfWork();
    var secrets =
        new SecureTokenService() {
          public SecretToken issue() {
            throw new AssertionError("A reused token must not be rotated");
          }

          public String hash(String raw) {
            return original.refreshTokenHash();
          }
        };
    var useCase =
        new RefreshSessionUseCase(
            repository,
            null,
            null,
            secrets,
            null,
            work,
            Clock.fixed(now, ZoneOffset.UTC),
            Duration.ofDays(30),
            Duration.ofSeconds(10));

    assertThrows(RefreshTokenReuseApplicationException.class, () -> useCase.execute("reused"));
    assertEquals(2, work.executions);
    assertEquals(1, repository.familyRevocations);
    assertEquals(original.familyId(), repository.revokedFamily);
  }

  private static final class CountingUnitOfWork implements UnitOfWork {
    private int executions;

    public <T> T execute(Supplier<T> operation) {
      executions++;
      return operation.get();
    }
  }

  private static final class SessionRepository implements UserSessionRepository {
    private final UserSession rotated;
    private int familyRevocations;
    private UUID revokedFamily;

    private SessionRepository(UserSession rotated) {
      this.rotated = rotated;
    }

    public Optional<UserSession> findByRefreshTokenHash(String hash) {
      return Optional.of(rotated);
    }

    public Optional<UserSession> findById(UserSessionId id) {
      return Optional.empty();
    }

    public List<UserSession> findByUser(UserId id) {
      return List.of();
    }

    public UserSession save(UserSession session) {
      return session;
    }

    public void saveRotation(UserSession oldSession, UserSession successor) {
      throw new AssertionError("A reused token must not be rotated");
    }

    public void revokeFamily(UUID familyId, Instant at, String status) {
      familyRevocations++;
      revokedFamily = familyId;
      assertEquals("COMPROMISED", status);
    }

    public void revokeAllForUser(UserId id, Instant at) {}

    public boolean isActive(UserSessionId sessionId, UserId userId, Instant at) {
      return false;
    }
  }
}
