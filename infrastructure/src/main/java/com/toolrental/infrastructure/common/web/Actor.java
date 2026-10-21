package com.toolrental.infrastructure.common.web;

import java.security.Principal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class Actor {
  private Actor() {}

  public static UUID id(Principal principal) {
    if (principal == null)
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, "Authenticated principal required");
    try {
      return UUID.fromString(principal.getName());
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, "Principal requires a UUID subject");
    }
  }
}
