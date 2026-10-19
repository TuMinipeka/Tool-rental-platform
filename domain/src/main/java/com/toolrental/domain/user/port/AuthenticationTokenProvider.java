package com.toolrental.domain.user.port;

import com.toolrental.domain.user.model.valueobject.IssuedToken;
import com.toolrental.domain.user.model.valueobject.PasswordResetSubject;
import com.toolrental.domain.user.model.valueobject.UserId;
import java.time.Instant;
import java.util.Set;

public interface AuthenticationTokenProvider {
  IssuedToken issueAccessToken(UserId userId, Set<String> roles, Instant issuedAt);

  IssuedToken issuePasswordResetToken(UserId userId, String passwordHash, Instant issuedAt);

  PasswordResetSubject verifyPasswordResetToken(String token);

  boolean matchesCurrentPassword(PasswordResetSubject subject, String currentPasswordHash);
}
