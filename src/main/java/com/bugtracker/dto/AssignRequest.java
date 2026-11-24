package com.bugtracker.dto;

/** Body of the "assign ticket" call. assigneeId = null means "make it unassigned". */
public record AssignRequest(Long assigneeId) {
}
