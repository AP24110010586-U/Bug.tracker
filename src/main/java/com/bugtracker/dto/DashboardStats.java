package com.bugtracker.dto;

import java.util.List;
import java.util.Map;

/** All the numbers shown on the Dashboard and Reports pages. */
public record DashboardStats(
        long total,
        long open,
        long inProgress,
        long resolved,
        long closed,
        long unassigned,
        double resolutionRate,
        Map<String, Long> byStatus,
        Map<String, Long> byPriority,
        Map<String, Long> bySeverity,
        Map<String, Long> byDeveloper,
        List<TicketResponse> recentTickets) {
}
