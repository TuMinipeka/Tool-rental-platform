package com.toolrental.domain.user.port;

import com.toolrental.domain.user.model.valueobject.IssuedToken;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.domain.usersession.model.valueobject.UserSessionId;
import java.time.Instant;
import java.util.Set;

public interface AuthenticationTokenProvider {
  IssuedToken issueAccessToken(
      UserId userId, Set<String> roles, UserSessionId sessionId, Instant issuedAt);
}
