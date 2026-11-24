package com.bugtracker.exception;

import org.springframework.http.HttpStatus;

/** Thrown when the data sent by the user does not make sense (HTTP 400). */
public class BadRequestException extends BugTrackerException {

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
