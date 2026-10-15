package com.toolrental.domain.role.model.aggregate;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.role.event.RoleChanged;
import com.toolrental.domain.role.model.valueobject.RoleId;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class Role extends AggregateRoot {
  private final RoleId id;
  private final String code;
  private final String description;

  private Role(RoleId id, String code, String description) {
    Objects.requireNonNull(id);
    Objects.requireNonNull(code, "code is required");
    if (code != null && (code.isBlank() || code.length() > 40))
      throw new BusinessRuleException("Invalid code");
    Objects.requireNonNull(description, "description is required");
    if (description != null && (description.isBlank() || description.length() > 200))
      throw new BusinessRuleException("Invalid description");
    this.id = id;
    this.code = code;
    this.description = description;
  }

  public static Role restore(RoleId id, String code, String description) {
    return new Role(id, code, description);
  }

  public static Role register(RoleId id, String code, String description, Clock clock) {
    var result = new Role(id, code, description);
    result.recordEvent(new RoleChanged(id, Instant.now(clock)));
    return result;
  }

  public RoleId id() {
    return id;
  }

  public String code() {
    return code;
  }

  public String description() {
    return description;
  }

  public Role update(String code, String description, Clock clock) {
    if (!this.code.equals(code)) throw new BusinessRuleException("Role code is immutable");
    var changed = new Role(id, code, description);
    changed.recordEvent(new RoleChanged(id, Instant.now(clock)));
    return changed;
  }
}
