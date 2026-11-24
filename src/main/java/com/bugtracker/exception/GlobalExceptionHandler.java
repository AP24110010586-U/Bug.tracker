package com.bugtracker.exception;

import com.bugtracker.dto.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * EXCEPTION HANDLING in one place.
 * Whenever a controller or service throws an exception, Spring calls one of the methods
 * below and we turn it into a clean JSON message such as {"status":404,"message":"Ticket not found"}.
 * The JavaScript frontend shows that message to the user.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Handles all our own exceptions (404, 400, 403, 401). POLYMORPHISM: one method, many exception types. */
    @ExceptionHandler(BugTrackerException.class)
    public ResponseEntity<ApiError> handleBugTrackerException(BugTrackerException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(new ApiError(ex.getStatus().value(), ex.getMessage()));
    }

    /** A form field failed validation, e.g. an empty title. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = "Please check the form and try again.";
        FieldError fieldError = ex.getBindingResult().getFieldError();
        if (fieldError != null && fieldError.getDefaultMessage() != null) {
            message = fieldError.getDefaultMessage();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(400, message));
    }

    /** The JSON sent by the browser was broken or contained an unknown value (e.g. priority "SUPER"). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(400, "The request contains invalid data."));
    }

    /** A URL parameter had the wrong type, e.g. ?status=BANANA. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(400, "Invalid value for '" + ex.getName() + "'."));
    }

    /** Safety net for anything unexpected. */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleUnexpected(RuntimeException ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(500, "Something went wrong on the server."));
    }
}
