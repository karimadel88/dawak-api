package com.dawak.api.common.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ProblemDetail handleApiException(ApiException exception, HttpServletRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(exception.getStatus(), exception.getMessage());
        problem.setType(URI.create("https://api.dawak.example/problems/" + exception.getCode().toLowerCase().replace('_', '-')));
        problem.setTitle(exception.getStatus().getReasonPhrase());
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", exception.getCode());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        var fields = new LinkedHashMap<String, String>();
        exception.getBindingResult().getFieldErrors().forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "One or more fields are invalid.");
        problem.setType(URI.create("https://api.dawak.example/problems/validation-error"));
        problem.setTitle("Validation failed");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "VALIDATION_ERROR");
        problem.setProperty("fieldErrors", fields);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
