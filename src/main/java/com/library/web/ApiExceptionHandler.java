package com.library.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("error", message(exception, "Invalid request.")));
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    ResponseEntity<Map<String, String>> conflict(Exception exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "This record already exists or is still referenced by library history."));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String, String>> responseStatus(ResponseStatusException exception) {
        String reason = exception.getReason() == null ? "Request failed." : exception.getReason();
        return ResponseEntity.status(exception.getStatusCode()).body(Map.of("error", reason));
    }

    @ExceptionHandler(SQLException.class)
    ResponseEntity<Map<String, String>> databaseFailure(SQLException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Database operation failed. Check the connection and schema configuration."));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> failure(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", message(exception, "The request could not be completed.")));
    }

    private String message(Exception exception, String fallback) {
        String text = exception.getMessage();
        return text == null || text.isBlank() ? fallback : text;
    }
}
