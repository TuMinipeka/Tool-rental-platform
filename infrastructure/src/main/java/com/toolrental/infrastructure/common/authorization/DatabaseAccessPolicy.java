package com.toolrental.infrastructure.common.authorization;

import com.toolrental.application.common.exception.AccessDeniedApplicationException;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.infrastructure.user.adapters.out.persistence.repository.UserJpaRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DatabaseAccessPolicy implements AccessPolicy {
  private final UserJpaRepository users;

  public DatabaseAccessPolicy(UserJpaRepository users) {
    this.users = users;
  }

  public void requireActive(UUID actor) {
    if (actor == null
        || users.findById(actor).filter(u -> "ACTIVE".equals(u.getAccountStatus())).isEmpty())
      throw new AccessDeniedApplicationException();
  }

  public void requireAdministrator(UUID actor) {
    requireRole(actor, "ADMIN");
  }

  public void requireRole(UUID actor, String role) {
    if (actor == null || !users.hasActiveRole(actor, role))
      throw new AccessDeniedApplicationException();
  }

  public void requireOwnerOrAdministrator(UUID actor, UUID owner) {
    requireActive(actor);
    if (!actor.equals(owner) && !users.hasActiveRole(actor, "ADMIN"))
      throw new AccessDeniedApplicationException();
  }
}
