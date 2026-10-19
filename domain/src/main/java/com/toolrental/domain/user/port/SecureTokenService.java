package com.toolrental.domain.user.port;

import com.toolrental.domain.user.model.valueobject.SecretToken;

public interface SecureTokenService {
  SecretToken issue();

  String hash(String rawToken);
}
