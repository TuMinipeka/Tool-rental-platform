package com.toolrental.application.user.usecase;

import com.toolrental.application.user.command.RegisterSupplierAccountCommand;
import com.toolrental.application.user.command.RegisterUserCommand;
import com.toolrental.application.user.dto.UserResponse;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.role.port.repository.RoleRepository;
import com.toolrental.domain.supplier.model.aggregate.Supplier;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import com.toolrental.domain.supplier.port.repository.SupplierRepository;
import com.toolrental.domain.user.model.aggregate.User;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.domain.user.port.PasswordHasher;
import com.toolrental.domain.user.port.repository.UserRepository;
import com.toolrental.domain.userrole.model.aggregate.UserRole;
import com.toolrental.domain.userrole.model.valueobject.UserRoleId;
import com.toolrental.domain.userrole.port.repository.UserRoleRepository;
import java.time.Clock;
import java.util.UUID;

public final class CreateManagedAccountUseCase {
  private final UserRepository users;
  private final SupplierRepository suppliers;
  private final RoleRepository roles;
  private final UserRoleRepository assignments;
  private final PasswordHasher passwords;
  private final UnitOfWork work;
  private final AccessPolicy access;
  private final Clock clock;

  public CreateManagedAccountUseCase(
      UserRepository users,
      SupplierRepository suppliers,
      RoleRepository roles,
      UserRoleRepository assignments,
      PasswordHasher passwords,
      UnitOfWork work,
      AccessPolicy access,
      Clock clock) {
    this.users = users;
    this.suppliers = suppliers;
    this.roles = roles;
    this.assignments = assignments;
    this.passwords = passwords;
    this.work = work;
    this.access = access;
    this.clock = clock;
  }

  private User create(RegisterUserCommand command, String role) {
    if (command.password() == null
        || command.password().length() < 12
        || command.password().length() > 72)
      throw new IllegalArgumentException("Password length must be between 12 and 72 characters");
    var email = command.email().trim().toLowerCase(java.util.Locale.ROOT);
    if (users.findByEmail(email).isPresent())
      throw new com.toolrental.domain.common.exception.BusinessRuleException(
          "Email is already registered");
    var assigned =
        roles
            .findByCode(role)
            .orElseThrow(
                () ->
                    new com.toolrental.application.common.exception
                        .ReferenceNotFoundApplicationException("Role"));
    var user =
        users.save(
            User.register(
                UserId.generate(),
                email,
                command.displayName().trim(),
                passwords.hash(command.password()),
                "ACTIVE",
                java.time.Instant.now(clock),
                clock));
    assignments.save(UserRole.register(new UserRoleId(user.id(), assigned.id()), clock));
    return user;
  }

  public UserResponse administrator(UUID actor, RegisterUserCommand command) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          return UserResponse.from(create(command, "ADMIN"));
        });
  }

  public UserResponse supplier(UUID actor, RegisterSupplierAccountCommand command) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          var user =
              create(
                  new RegisterUserCommand(
                      command.email(), command.displayName(), command.password()),
                  "SUPPLIER");
          suppliers.save(
              Supplier.register(
                  new SupplierId(user.id().value()),
                  command.documentType(),
                  command.documentNumber(),
                  command.businessName(),
                  clock));
          return UserResponse.from(user);
        });
  }
}
