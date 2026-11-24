package com.bugtracker.security;

import com.bugtracker.model.Status;
import com.bugtracker.model.Ticket;
import com.bugtracker.model.User;

import java.util.Collections;
import java.util.List;

/**
 * Developer: works on the tickets assigned to him/her.
 * Can move them Open -> In Progress -> Resolved, but cannot close them (testers verify and close).
 */
public class DeveloperPermissions extends AbstractRolePermissions {

    @Override
    public List<Status> allowedStatuses(User user, Ticket ticket) {
        if (isAssignee(user, ticket) && ticket.getStatus() != Status.CLOSED) {
            return List.of(Status.OPEN, Status.IN_PROGRESS, Status.RESOLVED);
        }
        return Collections.emptyList();
    }
}
