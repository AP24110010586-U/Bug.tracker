package com.bugtracker.security;

import com.bugtracker.model.Status;
import com.bugtracker.model.Ticket;
import com.bugtracker.model.User;

import java.util.Collections;
import java.util.List;

/**
 * ABSTRACT CLASS: implements the "safe" default answers (mostly NO) and the helper methods
 * shared by all roles. Each role class extends this and only overrides what it is allowed to do.
 */
public abstract class AbstractRolePermissions implements RolePermissions {

    @Override
    public boolean canReportBug() {
        return true; // everybody may report a bug
    }

    @Override
    public boolean canEditTicket(User user, Ticket ticket) {
        return false;
    }

    @Override
    public boolean canDeleteTicket(User user, Ticket ticket) {
        return false;
    }

    @Override
    public boolean canAssignTicket() {
        return false;
    }

    @Override
    public List<Status> allowedStatuses(User user, Ticket ticket) {
        return Collections.emptyList();
    }

    /** Is this user the person who reported the ticket? */
    protected boolean isReporter(User user, Ticket ticket) {
        return ticket.getReporter() != null && ticket.getReporter().getId().equals(user.getId());
    }

    /** Is the ticket assigned to this user? */
    protected boolean isAssignee(User user, Ticket ticket) {
        return ticket.getAssignee() != null && ticket.getAssignee().getId().equals(user.getId());
    }
}
