package com.toolrental.infrastructure.common.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PrincipalInterceptor implements HandlerInterceptor {
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws java.io.IOException {
    if (request.getUserPrincipal() == null) {
      response.sendError(401, "Authenticated principal required");
      return false;
    }
    Actor.id(request.getUserPrincipal());
    return true;
  }
}
