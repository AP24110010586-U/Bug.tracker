package com.bugtracker.dto;

import com.bugtracker.model.User;

/** A user as the browser sees it. Notice there is NO password field here. */
public record UserResponse(
        Long id,
        String username,
        String fullName,
        String email,
        String role,
        String roleLabel) {

    /** Converts a database User object into a UserResponse. */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                user.getRole().getLabel());
    }
}
