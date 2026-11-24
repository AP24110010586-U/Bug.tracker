package com.bugtracker.exception;

import org.springframework.http.HttpStatus;

/** Thrown when the logged-in user's role does not allow the action (HTTP 403). */
public class ForbiddenException extends BugTrackerException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
