package com.toolrental.domain.user.model.aggregate;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.user.event.UserChanged;
import com.toolrental.domain.user.model.valueobject.UserId;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public final class User extends AggregateRoot {
  private final UserId id;
  private final String email;
  private final String displayName;
  private final String passwordHash;
  private final String accountStatus;
  private final Instant registeredAt;

  private User(
      UserId id,
      String email,
      String displayName,
      String passwordHash,
      String accountStatus,
      Instant registeredAt) {
    Objects.requireNonNull(id);
    Objects.requireNonNull(email, "email is required");
    if (email != null && (email.isBlank() || email.length() > 320))
      throw new BusinessRuleException("Invalid email");
    Objects.requireNonNull(displayName, "displayName is required");
    if (displayName != null && (displayName.isBlank() || displayName.length() > 200))
      throw new BusinessRuleException("Invalid displayName");
    Objects.requireNonNull(passwordHash, "passwordHash is required");
    if (passwordHash != null && (passwordHash.isBlank() || passwordHash.length() > 255))
      throw new BusinessRuleException("Invalid passwordHash");
    Objects.requireNonNull(accountStatus, "accountStatus is required");
    if (accountStatus != null && (accountStatus.isBlank() || accountStatus.length() > 20))
      throw new BusinessRuleException("Invalid accountStatus");
    if (!Set.of("ACTIVE", "SUSPENDED", "DISABLED").contains(accountStatus))
      throw new BusinessRuleException("Invalid accountStatus");
    Objects.requireNonNull(registeredAt, "registeredAt is required");
    if (!email.matches("[^ @]+@[^ @]+[.][^ @]+")) throw new BusinessRuleException("Invalid email");
    this.id = id;
    this.email = email;
    this.displayName = displayName;
    this.passwordHash = passwordHash;
    this.accountStatus = accountStatus;
    this.registeredAt = registeredAt;
  }

  public static User restore(
      UserId id,
      String email,
      String displayName,
      String passwordHash,
      String accountStatus,
      Instant registeredAt) {
    return new User(id, email, displayName, passwordHash, accountStatus, registeredAt);
  }

  public static User register(
      UserId id,
      String email,
      String displayName,
      String passwordHash,
      String accountStatus,
      Instant registeredAt,
      Clock clock) {
    var result = new User(id, email, displayName, passwordHash, accountStatus, registeredAt);
    result.recordEvent(new UserChanged(id, Instant.now(clock)));
    return result;
  }

  public UserId id() {
    return id;
  }

  public String email() {
    return email;
  }

  public String displayName() {
    return displayName;
  }

  public String passwordHash() {
    return passwordHash;
  }

  public String accountStatus() {
    return accountStatus;
  }

  public Instant registeredAt() {
    return registeredAt;
  }

  public User transition(String next, Clock clock) {
    if (!(("ACTIVE".equals(accountStatus) && Set.of("SUSPENDED", "DISABLED").contains(next))
        || ("SUSPENDED".equals(accountStatus) && Set.of("ACTIVE", "DISABLED").contains(next))))
      throw new BusinessRuleException("Invalid User transition");
    var result = new User(id, email, displayName, passwordHash, next, registeredAt);
    result.recordEvent(new UserChanged(id, Instant.now(clock)));
    return result;
  }

  public User changePassword(String newPasswordHash, Clock clock) {
    var result = new User(id, email, displayName, newPasswordHash, accountStatus, registeredAt);
    result.recordEvent(new UserChanged(id, Instant.now(clock)));
    return result;
  }
}
