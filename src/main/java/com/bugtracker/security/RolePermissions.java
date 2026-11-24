package com.bugtracker.security;

import com.bugtracker.model.Status;
import com.bugtracker.model.Ticket;
import com.bugtracker.model.User;

import java.util.List;

/**
 * INTERFACE: a contract that says WHAT every role can be asked,
 * without saying HOW each role answers. See AdminPermissions, DeveloperPermissions
 * and TesterPermissions for the three different answers (POLYMORPHISM).
 */
public interface RolePermissions {

    boolean canReportBug();

    boolean canEditTicket(User user, Ticket ticket);

    boolean canDeleteTicket(User user, Ticket ticket);

    boolean canAssignTicket();

    /** The statuses this user is allowed to move this ticket to. Empty list = may not change status. */
    List<Status> allowedStatuses(User user, Ticket ticket);
}
