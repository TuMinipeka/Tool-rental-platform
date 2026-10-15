package com.toolrental.domain.common.port;

import java.util.UUID;

public interface AccessPolicy {
  void requireActive(UUID actor);

  void requireAdministrator(UUID actor);

  void requireOwnerOrAdministrator(UUID actor, UUID owner);

  void requireRole(UUID actor, String role);
}
