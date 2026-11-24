package com.bugtracker.dto;

import com.bugtracker.model.Priority;
import com.bugtracker.model.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** What the "Report a Bug" and "Edit Ticket" forms send. */
public record TicketRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must be at most 200 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 4000, message = "Description must be at most 4000 characters")
        String description,

        @NotNull(message = "Priority is required")
        Priority priority,

        @NotNull(message = "Severity is required")
        Severity severity) {
}
