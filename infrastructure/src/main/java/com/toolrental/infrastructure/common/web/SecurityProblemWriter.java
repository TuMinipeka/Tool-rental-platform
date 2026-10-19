package com.toolrental.infrastructure.common.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

@Component
public class SecurityProblemWriter implements AuthenticationEntryPoint, AccessDeniedHandler {
  private final ObjectMapper mapper;

  public SecurityProblemWriter(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void commence(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws java.io.IOException {
    write(request, response, HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED");
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      org.springframework.security.access.AccessDeniedException exception)
      throws java.io.IOException {
    write(
        request,
        response,
        HttpStatus.FORBIDDEN,
        exception instanceof CsrfException ? "CSRF_VALIDATION_FAILED" : "ACCESS_DENIED");
  }

  private void write(
      HttpServletRequest request, HttpServletResponse response, HttpStatus status, String code)
      throws java.io.IOException {
    var problem = ProblemDetail.forStatusAndDetail(status, status.getReasonPhrase());
    problem.setTitle(status.getReasonPhrase());
    problem.setInstance(URI.create(request.getRequestURI()));
    problem.setProperty("code", code);
    var suppliedTraceId = request.getHeader("X-Trace-Id");
    problem.setProperty(
        "traceId",
        suppliedTraceId == null || suppliedTraceId.isBlank()
            ? UUID.randomUUID().toString()
            : suppliedTraceId);
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    mapper.writeValue(response.getOutputStream(), problem);
  }
}
