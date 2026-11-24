package com.bugtracker.dto;

import jakarta.validation.constraints.NotBlank;

/** What the login form sends. */
public record LoginRequest(
        @NotBlank(message = "Username is required") String username,
        @NotBlank(message = "Password is required") String password) {
}
