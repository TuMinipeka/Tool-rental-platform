package com.toolrental.application;

import static org.junit.jupiter.api.Assertions.*;

import com.toolrental.application.user.exception.AuthenticationFailedApplicationException;
import com.toolrental.application.user.usecase.AuthenticateUserUseCase;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.user.model.aggregate.User;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.domain.user.port.PasswordHasher;
import com.toolrental.domain.user.port.repository.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class AuthenticateUserSecurityTest {
  @Test
  void unknownDisabledAndWrongPasswordAccountsAllVerifyAndFailGenerically() {
    for (var state : List.of("MISSING", "SUSPENDED", "ACTIVE")) {
      var checks = new java.util.ArrayList<String>();
      var passwords =
          new PasswordHasher() {
            public String hash(String password) {
              return "dummy-hash";
            }

            public boolean matches(String password, String hash) {
              checks.add(hash);
              return false;
            }
          };
      var users =
          new UserRepository() {
            public Optional<User> findByEmail(String email) {
              assertEquals("customer+tag@example.test", email);
              return state.equals("MISSING")
                  ? Optional.empty()
                  : Optional.of(
                      User.restore(
                          UserId.generate(),
                          email,
                          "Customer",
                          "stored-hash",
                          state,
                          Instant.now()));
            }

            public Optional<User> findById(UserId id) {
              throw new UnsupportedOperationException();
            }

            public List<User> findAll() {
              throw new UnsupportedOperationException();
            }

            public User save(User value) {
              throw new UnsupportedOperationException();
            }

            public void delete(UserId id) {
              throw new UnsupportedOperationException();
            }
          };
      var work =
          new UnitOfWork() {
            public <T> T execute(Supplier<T> operation) {
              return operation.get();
            }
          };
      var useCase =
          new AuthenticateUserUseCase(
              users,
              passwords,
              null,
              null,
              work,
              Clock.systemUTC(),
              null,
              null,
              Duration.ofDays(30));
      assertThrows(
          AuthenticationFailedApplicationException.class,
          () -> useCase.execute(" Customer+TAG@example.test ", "wrong-password", "test"));
      assertEquals(List.of(state.equals("MISSING") ? "dummy-hash" : "stored-hash"), checks);
    }
  }
}
