package com.bugtracker.exception;

import org.springframework.http.HttpStatus;

/** Thrown when the user is not logged in or the password is wrong (HTTP 401). */
public class UnauthorizedException extends BugTrackerException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
