package com.toolrental.domain.userrole.model.aggregate;

import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.userrole.event.UserRoleChanged;
import com.toolrental.domain.userrole.model.valueobject.UserRoleId;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class UserRole extends AggregateRoot {
  private final UserRoleId id;

  private UserRole(UserRoleId id) {
    Objects.requireNonNull(id);
    this.id = id;
  }

  public static UserRole restore(UserRoleId id) {
    return new UserRole(id);
  }

  public static UserRole register(UserRoleId id, Clock clock) {
    var result = new UserRole(id);
    result.recordEvent(new UserRoleChanged(id, Instant.now(clock)));
    return result;
  }

  public UserRoleId id() {
    return id;
  }
}
