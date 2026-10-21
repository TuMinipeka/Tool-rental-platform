package com.toolrental.infrastructure.common.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LegacyApiDeprecationInterceptor implements HandlerInterceptor {
  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    var path = request.getRequestURI();
    if (path.startsWith("/api/") && !path.startsWith("/api/v1/")) {
      response.setHeader("Deprecation", "true");
      response.setHeader("Link", "</api/v1" + path.substring(4) + ">; rel=\"successor-version\"");
    }
    return true;
  }
}
