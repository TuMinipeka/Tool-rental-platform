package com.toolrental.domain.user.port;

public interface PasswordHasher {
  String hash(String password);

  boolean matches(String password, String passwordHash);
}
