package com.bugtracker.dto;

import com.bugtracker.model.Status;
import jakarta.validation.constraints.NotNull;

/** Body of the "update status" call. */
public record StatusRequest(@NotNull(message = "Status is required") Status status) {
}
