package com.toolrental.domain.usersession.model.valueobject;

import com.toolrental.domain.usersession.model.aggregate.UserSession;

public record UserSessionRotation(UserSession rotated, UserSession successor) {}
