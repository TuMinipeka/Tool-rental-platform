package com.toolrental.infrastructure.usersession.adapters.in.rest;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public final class RefreshCookieFactory {
  private final Duration ttl;
  private final boolean secure;
  private final String sameSite;

  public RefreshCookieFactory(
      @Value("${auth.jwt.refresh-ttl:P30D}") Duration ttl,
      @Value("${auth.cookie.secure:false}") boolean secure,
      @Value("${auth.cookie.same-site:Lax}") String sameSite) {
    this.ttl = ttl;
    this.secure = secure;
    this.sameSite = sameSite;
  }

  public ResponseCookie issue(String value) {
    return base(value).maxAge(ttl).build();
  }

  public ResponseCookie clear() {
    return base("").maxAge(0).build();
  }

  private ResponseCookie.ResponseCookieBuilder base(String value) {
    return ResponseCookie.from("trp_refresh", value)
        .httpOnly(true)
        .secure(secure)
        .sameSite(sameSite)
        .path("/api");
  }
}
