package com.bugtracker.exception;

import org.springframework.http.HttpStatus;

/**
 * INHERITANCE + EXCEPTION HANDLING:
 * Base class for all of our own (custom) exceptions.
 * Each child class below only decides which HTTP status code goes with it.
 */
public class BugTrackerException extends RuntimeException {

    private final HttpStatus status;

    public BugTrackerException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
