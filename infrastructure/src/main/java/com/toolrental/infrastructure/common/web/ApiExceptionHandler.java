package com.toolrental.infrastructure.common.web;

import com.toolrental.application.common.exception.AccessDeniedApplicationException;
import com.toolrental.application.common.exception.ApplicationException;
import com.toolrental.domain.common.exception.DomainException;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(AccessDeniedApplicationException.class)
  ResponseEntity<Map<String, String>> forbidden(AccessDeniedApplicationException ex) {
    return error(403, "Access denied");
  }

  @ExceptionHandler(ApplicationException.class)
  ResponseEntity<Map<String, String>> missing(ApplicationException ex) {
    return error(404, ex.getMessage());
  }

  @ExceptionHandler(DomainException.class)
  ResponseEntity<Map<String, String>> conflict(DomainException ex) {
    return error(409, ex.getMessage());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<Map<String, String>> integrity(DataIntegrityViolationException ex) {
    return error(409, "Operation violates a stored reference or uniqueness constraint");
  }

  @ExceptionHandler({
    IllegalArgumentException.class,
    MethodArgumentNotValidException.class,
    HttpMessageNotReadableException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
  })
  ResponseEntity<Map<String, String>> invalid(Exception ex) {
    return error(400, "Invalid request");
  }

  private ResponseEntity<Map<String, String>> error(int status, String message) {
    return ResponseEntity.status(status).body(Map.of("message", message));
  }
}
