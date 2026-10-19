package com.toolrental.application.user.usecase;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.user.command.RegisterUserCommand;
import com.toolrental.application.user.dto.UserResponse;
import com.toolrental.application.user.exception.UserNotFoundApplicationException;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.user.model.aggregate.User;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.domain.user.port.PasswordHasher;
import com.toolrental.domain.user.port.repository.UserRepository;
import com.toolrental.domain.usersession.port.repository.UserSessionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ManageUserUseCase {
  private final UserRepository repository;
  private final UnitOfWork work;
  private final AccessPolicy access;
  private final WorkflowReferences references;
  private final Clock clock;
  private final PasswordHasher hasher;
  private final UserSessionRepository sessions;

  public ManageUserUseCase(
      UserRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      PasswordHasher hasher,
      UserSessionRepository sessions) {
    this.repository = repository;
    this.work = work;
    this.access = access;
    this.references = references;
    this.clock = clock;
    this.hasher = hasher;
    this.sessions = sessions;
  }

  private User load(UserId id) {
    return repository.findById(id).orElseThrow(UserNotFoundApplicationException::new);
  }

  public UserResponse find(UUID actor, UserId id) {
    return work.execute(
        () -> {
          var value = load(id);
          access.requireOwnerOrAdministrator(actor, value.id().value());
          return UserResponse.from(value);
        });
  }

  public List<UserResponse> list(UUID actor) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          return repository.findAll().stream().map(UserResponse::from).toList();
        });
  }

  public UserResponse register(UUID actor, RegisterUserCommand command) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          var id = UserId.generate();
          if (repository.findById(id).isPresent())
            throw new BusinessRuleException("User already exists");
          var value =
              User.register(
                  id,
                  command.email(),
                  command.displayName(),
                  hasher.hash(command.password()),
                  "ACTIVE",
                  Instant.now(clock),
                  clock);
          var saved = repository.save(value);
          return UserResponse.from(saved);
        });
  }

  public UserResponse transition(UUID actor, UserId id, String next) {
    return work.execute(
        () -> {
          var value = load(id);
          access.requireAdministrator(actor);
          var changed = repository.save(value.transition(next, clock));
          if (!"ACTIVE".equals(next)) sessions.revokeAllForUser(changed.id(), Instant.now(clock));
          return UserResponse.from(changed);
        });
  }
}
