package com.toolrental.domain.user.port;

import com.toolrental.domain.user.model.valueobject.UserId;
import java.util.Set;

public interface UserRoleReader {
  Set<String> findRoleCodes(UserId userId);
}
