package com.bugtracker.dto;

/**
 * DTO = Data Transfer Object: a small class that carries data between the browser and the server.
 * (A Java "record" is a short way to write a class with private final fields and getters.)
 */
public record ApiError(int status, String message) {
}
