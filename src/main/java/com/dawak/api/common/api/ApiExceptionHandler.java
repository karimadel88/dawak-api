package com.dawak.api.common.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.slf4j.MDC;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ProblemDetail handleApiException(ApiException exception, HttpServletRequest request) {
        if (exception.getStatus().is5xxServerError()) {
            log.error("API dependency failure code={} status={} method={} path={}", exception.getCode(),
                    exception.getStatus().value(), request.getMethod(), request.getRequestURI(), exception);
        } else {
            log.warn("Handled API exception code={} status={} method={} path={} detail={}", exception.getCode(),
                    exception.getStatus().value(), request.getMethod(), request.getRequestURI(), exception.getMessage());
        }
        var problem = ProblemDetail.forStatusAndDetail(exception.getStatus(), exception.getMessage());
        problem.setType(URI.create("https://api.dawak.example/problems/" + exception.getCode().toLowerCase().replace('_', '-')));
        problem.setTitle(exception.getStatus().getReasonPhrase());
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", exception.getCode());
        problem.setProperty("timestamp", Instant.now());
        addRequestId(problem);
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        log.warn("Handled validation exception method={} path={} fieldErrorCount={}", request.getMethod(),
                request.getRequestURI(), exception.getBindingResult().getFieldErrorCount());
        var fields = new LinkedHashMap<String, String>();
        exception.getBindingResult().getFieldErrors().forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "One or more fields are invalid.");
        problem.setType(URI.create("https://api.dawak.example/problems/validation-error"));
        problem.setTitle("Validation failed");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "VALIDATION_ERROR");
        problem.setProperty("fieldErrors", fields);
        problem.setProperty("timestamp", Instant.now());
        addRequestId(problem);
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleUnreadableBody(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return clientProblem(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_BODY",
                "Request body is missing or malformed.", request, exception);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    ProblemDetail handleMissingHeader(MissingRequestHeaderException exception, HttpServletRequest request) {
        return clientProblem(HttpStatus.BAD_REQUEST, "MISSING_REQUIRED_HEADER",
                "Required header is missing: " + exception.getHeaderName(), request, exception);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ProblemDetail handleMissingParameter(MissingServletRequestParameterException exception, HttpServletRequest request) {
        return clientProblem(HttpStatus.BAD_REQUEST, "MISSING_REQUIRED_PARAMETER",
                "Required parameter is missing: " + exception.getParameterName(), request, exception);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ProblemDetail handleUnsupportedMediaType(HttpMediaTypeNotSupportedException exception, HttpServletRequest request) {
        return clientProblem(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE",
                "Upload content must use application/octet-stream.", request, exception);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ProblemDetail handleOversizedUpload(MaxUploadSizeExceededException exception, HttpServletRequest request) {
        return clientProblem(HttpStatus.PAYLOAD_TOO_LARGE, "PRESCRIPTION_FILE_TOO_LARGE",
                "Uploaded content exceeds the configured maximum size.", request, exception);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleAccessDenied(AccessDeniedException exception, HttpServletRequest request) {
        log.warn("Access denied method={} path={}", request.getMethod(), request.getRequestURI());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
                "You do not have permission to perform this action.");
        problem.setType(URI.create("https://api.dawak.example/problems/access-denied"));
        problem.setTitle("Forbidden");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "ACCESS_DENIED");
        problem.setProperty("timestamp", Instant.now());
        addRequestId(problem);
        return problem;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception method={} path={}", request.getMethod(), request.getRequestURI(), exception);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred.");
        problem.setType(URI.create("https://api.dawak.example/problems/internal-server-error"));
        problem.setTitle("Internal server error");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "INTERNAL_SERVER_ERROR");
        problem.setProperty("timestamp", Instant.now());
        addRequestId(problem);
        return problem;
    }

    private ProblemDetail clientProblem(HttpStatus status, String code, String detail,
                                        HttpServletRequest request, Exception exception) {
        log.warn("Rejected request code={} status={} method={} path={} reason={}", code, status.value(),
                request.getMethod(), request.getRequestURI(), exception.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("https://api.dawak.example/problems/" + code.toLowerCase().replace('_', '-')));
        problem.setTitle(status.getReasonPhrase());
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", code);
        problem.setProperty("timestamp", Instant.now());
        addRequestId(problem);
        return problem;
    }

    private void addRequestId(ProblemDetail problem) {
        String requestId = MDC.get("requestId");
        if (requestId != null) problem.setProperty("requestId", requestId);
    }
}
