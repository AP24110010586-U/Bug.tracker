package com.bugtracker.security;

import com.bugtracker.model.Status;
import com.bugtracker.model.Ticket;
import com.bugtracker.model.User;

import java.util.List;

/**
 * Tester: reports bugs, edits his/her own tickets, deletes own tickets that are still Open,
 * and verifies fixes by Closing a ticket or re-opening it.
 */
public class TesterPermissions extends AbstractRolePermissions {

    @Override
    public boolean canEditTicket(User user, Ticket ticket) {
        return isReporter(user, ticket);
    }

    @Override
    public boolean canDeleteTicket(User user, Ticket ticket) {
        return isReporter(user, ticket) && ticket.getStatus() == Status.OPEN;
    }

    @Override
    public List<Status> allowedStatuses(User user, Ticket ticket) {
        return List.of(Status.OPEN, Status.CLOSED);
    }
}
