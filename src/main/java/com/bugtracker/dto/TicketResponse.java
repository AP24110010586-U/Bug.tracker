package com.bugtracker.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A ticket as the browser sees it.
 * The last four fields tell the frontend what the CURRENT user may do with this ticket,
 * so the buttons can be shown or hidden (the server still double-checks every action).
 */
public record TicketResponse(
        Long id,
        String ticketCode,
        String title,
        String description,
        String priority,
        String severity,
        String status,
        UserResponse reporter,
        UserResponse assignee,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean canEdit,
        boolean canDelete,
        boolean canAssign,
        List<String> allowedStatuses) {
}
