package com.toolrental.domain.user.port;

public interface PasswordHasher {
  String hash(String password);
}
