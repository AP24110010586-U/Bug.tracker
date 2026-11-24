package com.bugtracker.exception;

import org.springframework.http.HttpStatus;

/** Thrown when a ticket or user with the requested id does not exist (HTTP 404). */
public class ResourceNotFoundException extends BugTrackerException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
