package com.bugtracker.service;

import com.bugtracker.dto.DashboardStats;
import com.bugtracker.dto.TicketRequest;
import com.bugtracker.dto.TicketResponse;
import com.bugtracker.model.Priority;
import com.bugtracker.model.Severity;
import com.bugtracker.model.Status;
import com.bugtracker.model.User;

import java.util.List;

/**
 * INTERFACE: lists everything the ticket feature can do.
 * The controller only depends on this interface; the real work is in TicketServiceImpl.
 */
public interface TicketService {

    /** Search + filter. Any parameter that is null is ignored. assigneeId = 0 means "unassigned". */
    List<TicketResponse> search(User viewer, String keyword, Status status, Priority priority,
                                Severity severity, Long assigneeId);

    TicketResponse getById(User viewer, Long id);

    TicketResponse create(User reporter, TicketRequest request);

    TicketResponse update(User viewer, Long id, TicketRequest request);

    void delete(User viewer, Long id);

    TicketResponse changeStatus(User viewer, Long id, Status newStatus);

    /** assigneeId = null removes the current assignee. */
    TicketResponse assign(User viewer, Long id, Long assigneeId);

    DashboardStats getStats(User viewer);
}
