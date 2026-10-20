package com.toolrental.application.user.usecase;

import com.toolrental.application.common.exception.ReferenceNotFoundApplicationException;
import com.toolrental.application.supplier.dto.SupplierRegistrationResponse;
import com.toolrental.application.user.command.RegisterSupplierAccountCommand;
import com.toolrental.domain.common.exception.BusinessRuleException;
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
import java.time.Instant;
import java.util.Locale;

public final class RegisterSupplierAccountUseCase {
  private final UserRepository users;
  private final SupplierRepository suppliers;
  private final RoleRepository roles;
  private final UserRoleRepository assignments;
  private final PasswordHasher passwords;
  private final UnitOfWork work;
  private final Clock clock;

  public RegisterSupplierAccountUseCase(
      UserRepository users,
      SupplierRepository suppliers,
      RoleRepository roles,
      UserRoleRepository assignments,
      PasswordHasher passwords,
      UnitOfWork work,
      Clock clock) {
    this.users = users;
    this.suppliers = suppliers;
    this.roles = roles;
    this.assignments = assignments;
    this.passwords = passwords;
    this.work = work;
    this.clock = clock;
  }

  public SupplierRegistrationResponse execute(RegisterSupplierAccountCommand command) {
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
          users.save(
              User.register(
                  userId,
                  email,
                  command.displayName(),
                  passwords.hash(command.password()),
                  "SUSPENDED",
                  now,
                  clock));
          var supplier =
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
          return new SupplierRegistrationResponse(
              userId.value(), "SUSPENDED", supplier.businessName());
        });
  }
}
