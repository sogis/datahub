package ch.so.agi.datahub.controller;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import ch.so.agi.datahub.model.ApiError;
import ch.so.agi.datahub.model.ApiErrorDetail;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ApiExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<ApiErrorDetail> details = exception.getBindingResult().getFieldErrors().stream()
                .map(this::toDetail)
                .toList();
        ApiError error = new ApiError(
                exception.getClass().getCanonicalName(),
                "Validation failed.",
                Instant.now(),
                request.getRequestURI(),
                details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException exception, HttpServletRequest request) {
        ApiError error = new ApiError(
                exception.getClass().getCanonicalName(),
                "Request body is not readable.",
                Instant.now(),
                request.getRequestURI(),
                null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({Exception.class, RuntimeException.class})
    public ResponseEntity<ApiError> handleUnhandled(Exception exception, HttpServletRequest request) {
        logger.error("Unhandled exception", exception);
        ApiError error = new ApiError(
                exception.getClass().getCanonicalName(),
                "Please contact service provider.",
                Instant.now(),
                request.getRequestURI(),
                null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private ApiErrorDetail toDetail(FieldError error) {
        return new ApiErrorDetail(error.getField(), error.getDefaultMessage());
    }
}
