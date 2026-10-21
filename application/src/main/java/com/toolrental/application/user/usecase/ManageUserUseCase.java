package com.toolrental.application.user.usecase;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.user.command.ChangePasswordCommand;
import com.toolrental.application.user.command.RegisterUserCommand;
import com.toolrental.application.user.command.UpdateUserProfileCommand;
import com.toolrental.application.user.dto.UserResponse;
import com.toolrental.application.user.exception.AuthenticationFailedApplicationException;
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
import java.util.Locale;
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

  public UserResponse updateProfile(UUID actor, UpdateUserProfileCommand command) {
    return work.execute(
        () -> {
          var id = new UserId(actor);
          var value = load(id);
          var email = command.email().trim().toLowerCase(Locale.ROOT);
          var duplicate = repository.findByEmail(email);
          if (duplicate.isPresent() && !duplicate.get().id().equals(id))
            throw new BusinessRuleException("Email is already registered");
          return UserResponse.from(
              repository.save(value.updateProfile(email, command.displayName().trim(), clock)));
        });
  }

  public UserResponse updateManagedProfile(
      UUID actor, UserId id, UpdateUserProfileCommand command) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          var value = load(id);
          var email = command.email().trim().toLowerCase(Locale.ROOT);
          var duplicate = repository.findByEmail(email);
          if (duplicate.isPresent() && !duplicate.get().id().equals(id))
            throw new BusinessRuleException("Email is already registered");
          return UserResponse.from(
              repository.save(value.updateProfile(email, command.displayName().trim(), clock)));
        });
  }

  public UserResponse changePassword(UUID actor, ChangePasswordCommand command) {
    return work.execute(
        () -> {
          var value = load(new UserId(actor));
          if (!hasher.matches(command.currentPassword(), value.passwordHash()))
            throw new AuthenticationFailedApplicationException();
          if (hasher.matches(command.newPassword(), value.passwordHash()))
            throw new BusinessRuleException("New password must be different");
          var changed = repository.save(value.changePassword(hasher.hash(command.newPassword()), clock));
          sessions.revokeAllForUser(changed.id(), Instant.now(clock));
          return UserResponse.from(changed);
        });
  }
}
