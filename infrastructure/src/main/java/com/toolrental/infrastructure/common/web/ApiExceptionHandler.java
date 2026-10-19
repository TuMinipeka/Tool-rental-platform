package com.toolrental.infrastructure.common.web;

import com.toolrental.application.common.exception.AccessDeniedApplicationException;
import com.toolrental.application.common.exception.ApplicationException;
import com.toolrental.application.common.exception.AvailabilityConflictApplicationException;
import com.toolrental.application.common.exception.IdempotencyConflictApplicationException;
import com.toolrental.application.passwordreset.exception.InvalidPasswordResetApplicationException;
import com.toolrental.application.user.exception.AuthenticationFailedApplicationException;
import com.toolrental.application.usersession.exception.InvalidSessionApplicationException;
import com.toolrental.application.usersession.exception.RefreshTokenReuseApplicationException;
import com.toolrental.domain.common.exception.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler({
    AuthenticationFailedApplicationException.class,
    InvalidSessionApplicationException.class
  })
  ResponseEntity<ProblemDetail> unauthorized(RuntimeException ex, HttpServletRequest request) {
    return problem(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", ex.getMessage(), request);
  }

  @ExceptionHandler(InvalidPasswordResetApplicationException.class)
  ResponseEntity<ProblemDetail> invalidReset(RuntimeException ex, HttpServletRequest request) {
    return problem(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD_RESET", ex.getMessage(), request);
  }

  @ExceptionHandler(AccessDeniedApplicationException.class)
  ResponseEntity<ProblemDetail> forbidden(RuntimeException ex, HttpServletRequest request) {
    return problem(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied", request);
  }

  @ExceptionHandler(RefreshTokenReuseApplicationException.class)
  ResponseEntity<ProblemDetail> reused(RuntimeException ex, HttpServletRequest request) {
    return problem(HttpStatus.CONFLICT, "REFRESH_TOKEN_REUSED", ex.getMessage(), request);
  }

  @ExceptionHandler(AvailabilityConflictApplicationException.class)
  ResponseEntity<ProblemDetail> unavailable(RuntimeException ex, HttpServletRequest request) {
    return problem(HttpStatus.CONFLICT, "AVAILABILITY_CONFLICT", ex.getMessage(), request);
  }

  @ExceptionHandler(IdempotencyConflictApplicationException.class)
  ResponseEntity<ProblemDetail> idempotency(RuntimeException ex, HttpServletRequest request) {
    return problem(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT", ex.getMessage(), request);
  }

  @ExceptionHandler(ApplicationException.class)
  ResponseEntity<ProblemDetail> missing(RuntimeException ex, HttpServletRequest request) {
    return problem(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
  }

  @ExceptionHandler(DomainException.class)
  ResponseEntity<ProblemDetail> conflict(RuntimeException ex, HttpServletRequest request) {
    return problem(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", ex.getMessage(), request);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ProblemDetail> integrity(RuntimeException ex, HttpServletRequest request) {
    var message = mostSpecificMessage(ex);
    if (message.contains("idempotency"))
      return problem(
          HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT", "Idempotency key was already used", request);
    return problem(
        HttpStatus.CONFLICT,
        "PERSISTENCE_CONFLICT",
        "Operation violates a stored reference or uniqueness constraint",
        request);
  }

  private String mostSpecificMessage(Throwable error) {
    var current = error;
    while (current.getCause() != null) current = current.getCause();
    return current.getMessage() == null ? "" : current.getMessage().toLowerCase();
  }

  @ExceptionHandler({
    IllegalArgumentException.class,
    MethodArgumentNotValidException.class,
    HttpMessageNotReadableException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
  })
  ResponseEntity<ProblemDetail> invalid(Exception ex, HttpServletRequest request) {
    return problem(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Invalid request", request);
  }

  private ResponseEntity<ProblemDetail> problem(
      HttpStatus status, String code, String detail, HttpServletRequest request) {
    var body =
        ProblemDetail.forStatusAndDetail(
            status, detail == null ? status.getReasonPhrase() : detail);
    body.setTitle(status.getReasonPhrase());
    body.setInstance(URI.create(request.getRequestURI()));
    body.setProperty("code", code);
    body.setProperty("traceId", traceId(request));
    return ResponseEntity.status(status).body(body);
  }

  private String traceId(HttpServletRequest request) {
    var value = request.getHeader("X-Trace-Id");
    return value == null || value.isBlank() ? UUID.randomUUID().toString() : value;
  }
}
