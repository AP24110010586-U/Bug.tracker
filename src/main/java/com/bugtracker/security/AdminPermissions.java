package com.bugtracker.security;

import com.bugtracker.model.Status;
import com.bugtracker.model.Ticket;
import com.bugtracker.model.User;

import java.util.List;

/** Admin: may do everything. */
public class AdminPermissions extends AbstractRolePermissions {

    @Override
    public boolean canEditTicket(User user, Ticket ticket) {
        return true;
    }

    @Override
    public boolean canDeleteTicket(User user, Ticket ticket) {
        return true;
    }

    @Override
    public boolean canAssignTicket() {
        return true;
    }

    @Override
    public List<Status> allowedStatuses(User user, Ticket ticket) {
        return List.of(Status.values());
    }
}
