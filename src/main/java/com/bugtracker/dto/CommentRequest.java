package com.bugtracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body of the "add comment" call. */
public record CommentRequest(
        @NotBlank(message = "Comment cannot be empty")
        @Size(max = 2000, message = "Comment must be at most 2000 characters")
        String content) {
}
