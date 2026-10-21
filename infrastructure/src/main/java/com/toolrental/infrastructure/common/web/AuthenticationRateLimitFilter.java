package com.toolrental.infrastructure.common.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthenticationRateLimitFilter extends OncePerRequestFilter {
  private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
  private final ObjectMapper mapper;
  private final Clock clock;
  private final int requestsPerMinute;

  public AuthenticationRateLimitFilter(
      ObjectMapper mapper,
      Clock clock,
      @Value("${auth.rate-limit.requests-per-minute:10}") int requestsPerMinute) {
    this.mapper = mapper;
    this.clock = clock;
    this.requestsPerMinute = requestsPerMinute;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    var path = request.getRequestURI();
    return !"POST".equals(request.getMethod())
        || !(path.endsWith("/auth/login") || path.endsWith("/auth/refresh"));
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    var now = Instant.now(clock);
    var key = request.getRemoteAddr() + ":" + request.getRequestURI();
    var window =
        windows.compute(
            key,
            (ignored, current) ->
                current == null || now.isAfter(current.started.plusSeconds(60))
                    ? new Window(now, 1)
                    : new Window(current.started, current.count + 1));
    if (window.count > requestsPerMinute) {
      var problem =
          ProblemDetail.forStatusAndDetail(
              HttpStatus.TOO_MANY_REQUESTS, "Too many authentication requests");
      problem.setInstance(URI.create(request.getRequestURI()));
      problem.setProperty("code", "RATE_LIMIT_EXCEEDED");
      problem.setProperty("traceId", UUID.randomUUID().toString());
      response.setStatus(429);
      response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
      response.setHeader("Retry-After", "60");
      mapper.writeValue(response.getOutputStream(), problem);
      return;
    }
    chain.doFilter(request, response);
  }

  private record Window(Instant started, int count) {}
}
