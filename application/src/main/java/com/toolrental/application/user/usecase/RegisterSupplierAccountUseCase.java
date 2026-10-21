package com.toolrental.application.user.usecase;

import com.toolrental.application.common.exception.ReferenceNotFoundApplicationException;
import com.toolrental.application.user.command.RegisterSupplierAccountCommand;
import com.toolrental.application.user.dto.AuthenticatedSession;
import com.toolrental.application.user.dto.AuthenticationResponse;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.role.port.repository.RoleRepository;
import com.toolrental.domain.supplier.model.aggregate.Supplier;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import com.toolrental.domain.supplier.port.repository.SupplierRepository;
import com.toolrental.domain.user.model.aggregate.User;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.domain.user.port.AuthenticationTokenProvider;
import com.toolrental.domain.user.port.PasswordHasher;
import com.toolrental.domain.user.port.SecureTokenService;
import com.toolrental.domain.user.port.repository.UserRepository;
import com.toolrental.domain.userrole.model.aggregate.UserRole;
import com.toolrental.domain.userrole.model.valueobject.UserRoleId;
import com.toolrental.domain.userrole.port.repository.UserRoleRepository;
import com.toolrental.domain.usersession.model.aggregate.UserSession;
import com.toolrental.domain.usersession.model.valueobject.UserSessionId;
import com.toolrental.domain.usersession.port.repository.UserSessionRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;

public final class RegisterSupplierAccountUseCase {
  private final UserRepository users;
  private final SupplierRepository suppliers;
  private final RoleRepository roles;
  private final UserRoleRepository assignments;
  private final PasswordHasher passwords;
  private final AuthenticationTokenProvider tokens;
  private final UnitOfWork work;
  private final Clock clock;
  private final UserSessionRepository sessions;
  private final SecureTokenService secrets;
  private final Duration refreshTtl;

  public RegisterSupplierAccountUseCase(
      UserRepository users,
      SupplierRepository suppliers,
      RoleRepository roles,
      UserRoleRepository assignments,
      PasswordHasher passwords,
      AuthenticationTokenProvider tokens,
      UnitOfWork work,
      Clock clock,
      UserSessionRepository sessions,
      SecureTokenService secrets,
      Duration refreshTtl) {
    this.users = users;
    this.suppliers = suppliers;
    this.roles = roles;
    this.assignments = assignments;
    this.passwords = passwords;
    this.tokens = tokens;
    this.work = work;
    this.clock = clock;
    this.sessions = sessions;
    this.secrets = secrets;
    this.refreshTtl = refreshTtl;
  }

  public AuthenticatedSession execute(RegisterSupplierAccountCommand command, String clientLabel) {
    return work.execute(
        () -> {
          var email = command.email().trim().toLowerCase(Locale.ROOT);
          if (users.findByEmail(email).isPresent()) {
            throw new BusinessRuleException("Email is already registered");
          }
          var duplicateDocument =
              suppliers.findAll().stream()
                  .anyMatch(
                      supplier ->
                          supplier.documentType().equals(command.documentType())
                              && supplier.documentNumber().equals(command.documentNumber()));
          if (duplicateDocument) {
            throw new BusinessRuleException("Supplier document is already registered");
          }

          var userId = UserId.generate();
          var now = Instant.now(clock);
          var user =
              users.save(
                  User.register(
                      userId,
                      email,
                      command.displayName(),
                      passwords.hash(command.password()),
                      "ACTIVE",
                      now,
                      clock));
          suppliers.save(
              Supplier.register(
                  new SupplierId(userId.value()),
                  command.documentType(),
                  command.documentNumber(),
                  command.businessName(),
                  clock));
          var supplierRole =
              roles
                  .findByCode("SUPPLIER")
                  .orElseThrow(() -> new ReferenceNotFoundApplicationException("Supplier role"));
          assignments.save(UserRole.register(new UserRoleId(userId, supplierRole.id()), clock));
          var assignedRoles = Set.of("SUPPLIER");
          var secret = secrets.issue();
          var sessionId = UserSessionId.generate();
          sessions.save(
              UserSession.start(
                  sessionId,
                  java.util.UUID.randomUUID(),
                  userId,
                  secret.hash(),
                  now,
                  now.plus(refreshTtl),
                  clientLabel));
          var token = tokens.issueAccessToken(userId, assignedRoles, sessionId, now);
          return new AuthenticatedSession(
              AuthenticationResponse.from(user, assignedRoles, token), secret.value());
        });
  }
}
