package com.bugtracker.repository;

import com.bugtracker.model.Priority;
import com.bugtracker.model.Severity;
import com.bugtracker.model.Status;
import com.bugtracker.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /** Newest tickets first. */
    List<Ticket> findAllByOrderByCreatedAtDesc();

    /** The five newest tickets (shown on the dashboard). */
    List<Ticket> findTop5ByOrderByCreatedAtDesc();

    long countByStatus(Status status);

    long countByPriority(Priority priority);

    long countBySeverity(Severity severity);

    /** Number of tickets assigned to one developer. */
    long countByAssigneeId(Long assigneeId);

    /** Number of tickets that nobody is working on yet. */
    long countByAssigneeIsNull();
}
