package com.bugtracker.service;

import com.bugtracker.dto.DashboardStats;
import com.bugtracker.dto.TicketRequest;
import com.bugtracker.dto.TicketResponse;
import com.bugtracker.dto.UserResponse;
import com.bugtracker.exception.BadRequestException;
import com.bugtracker.exception.ForbiddenException;
import com.bugtracker.exception.ResourceNotFoundException;
import com.bugtracker.model.Priority;
import com.bugtracker.model.Role;
import com.bugtracker.model.Severity;
import com.bugtracker.model.Status;
import com.bugtracker.model.Ticket;
import com.bugtracker.model.User;
import com.bugtracker.repository.CommentRepository;
import com.bugtracker.repository.TicketRepository;
import com.bugtracker.repository.UserRepository;
import com.bugtracker.security.PermissionFactory;
import com.bugtracker.security.RolePermissions;
import com.bugtracker.util.TicketCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * All the business rules for tickets live here.
 * @Transactional = if anything fails half way, all database changes of that method are undone.
 */
@Service
@Transactional
public class TicketServiceImpl implements TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             CommentRepository commentRepository,
                             UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    // ------------------------------------------------------------------ READ

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> search(User viewer, String keyword, Status status, Priority priority,
                                       Severity severity, Long assigneeId) {
        String text = (keyword == null) ? "" : keyword.trim().toLowerCase();

        List<TicketResponse> results = new ArrayList<>();
        // COLLECTIONS: we loop over a List of tickets and keep only the ones that match every filter.
        for (Ticket ticket : ticketRepository.findAllByOrderByCreatedAtDesc()) {
            if (status != null && ticket.getStatus() != status) {
                continue;
            }
            if (priority != null && ticket.getPriority() != priority) {
                continue;
            }
            if (severity != null && ticket.getSeverity() != severity) {
                continue;
            }
            if (!matchesAssignee(ticket, assigneeId)) {
                continue;
            }
            if (!text.isEmpty() && !matchesKeyword(ticket, text)) {
                continue;
            }
            results.add(toResponse(ticket, viewer));
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getById(User viewer, Long id) {
        return toResponse(findTicket(id), viewer);
    }

    // ---------------------------------------------------------------- CREATE

    @Override
    public TicketResponse create(User reporter, TicketRequest request) {
        RolePermissions permissions = PermissionFactory.forRole(reporter.getRole());
        if (!permissions.canReportBug()) {
            throw new ForbiddenException("Your role is not allowed to report bugs.");
        }

        Ticket ticket = new Ticket();
        ticket.setTitle(request.title().trim());
        ticket.setDescription(request.description().trim());
        ticket.setPriority(request.priority());
        ticket.setSeverity(request.severity());
        ticket.setStatus(Status.OPEN);
        ticket.setReporter(reporter);

        // 1st save: MySQL gives the ticket its unique id.
        Ticket saved = ticketRepository.save(ticket);
        // 2nd step: build the ticket number (BUG-1001 ...) from that id.
        saved.setTicketCode(TicketCodeGenerator.generate(saved.getId()));
        saved = ticketRepository.save(saved);

        log.info("Created ticket {}", saved.describe());
        return toResponse(saved, reporter);
    }

    // ---------------------------------------------------------------- UPDATE

    @Override
    public TicketResponse update(User viewer, Long id, TicketRequest request) {
        Ticket ticket = findTicket(id);
        RolePermissions permissions = PermissionFactory.forRole(viewer.getRole());
        if (!permissions.canEditTicket(viewer, ticket)) {
            throw new ForbiddenException("You are not allowed to edit this ticket.");
        }

        ticket.setTitle(request.title().trim());
        ticket.setDescription(request.description().trim());
        ticket.setPriority(request.priority());
        ticket.setSeverity(request.severity());
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);
        log.info("{} edited ticket {}", viewer.describe(), saved.describe());
        return toResponse(saved, viewer);
    }

    @Override
    public TicketResponse changeStatus(User viewer, Long id, Status newStatus) {
        Ticket ticket = findTicket(id);
        RolePermissions permissions = PermissionFactory.forRole(viewer.getRole());
        if (!permissions.allowedStatuses(viewer, ticket).contains(newStatus)) {
            throw new ForbiddenException("You are not allowed to change this ticket to " + newStatus.getLabel() + ".");
        }

        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);
        log.info("{} changed {} to {}", viewer.describe(), saved.describe(), newStatus.getLabel());
        return toResponse(saved, viewer);
    }

    @Override
    public TicketResponse assign(User viewer, Long id, Long assigneeId) {
        Ticket ticket = findTicket(id);
        RolePermissions permissions = PermissionFactory.forRole(viewer.getRole());
        if (!permissions.canAssignTicket()) {
            throw new ForbiddenException("Only an Admin can assign tickets.");
        }

        if (assigneeId == null) {
            ticket.setAssignee(null); // un-assign
        } else {
            User developer = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + assigneeId));
            if (developer.getRole() != Role.DEVELOPER) {
                throw new BadRequestException("Tickets can only be assigned to developers.");
            }
            ticket.setAssignee(developer);
        }
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);
        log.info("{} assigned {} to {}", viewer.describe(), saved.describe(),
                saved.getAssignee() == null ? "nobody" : saved.getAssignee().getFullName());
        return toResponse(saved, viewer);
    }

    // ---------------------------------------------------------------- DELETE

    @Override
    public void delete(User viewer, Long id) {
        Ticket ticket = findTicket(id);
        RolePermissions permissions = PermissionFactory.forRole(viewer.getRole());
        if (!permissions.canDeleteTicket(viewer, ticket)) {
            throw new ForbiddenException("You are not allowed to delete this ticket.");
        }

        commentRepository.deleteByTicketId(id); // remove the comments first (foreign key)
        ticketRepository.delete(ticket);
        log.info("{} deleted ticket {}", viewer.describe(), ticket.describe());
    }

    // ------------------------------------------------------------ STATISTICS

    @Override
    @Transactional(readOnly = true)
    public DashboardStats getStats(User viewer) {
        long total = ticketRepository.count();
        long open = ticketRepository.countByStatus(Status.OPEN);
        long inProgress = ticketRepository.countByStatus(Status.IN_PROGRESS);
        long resolved = ticketRepository.countByStatus(Status.RESOLVED);
        long closed = ticketRepository.countByStatus(Status.CLOSED);
        long unassigned = ticketRepository.countByAssigneeIsNull();

        // COLLECTIONS: LinkedHashMap keeps the items in the order we put them in.
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (Status s : Status.values()) {
            byStatus.put(s.name(), ticketRepository.countByStatus(s));
        }

        Map<String, Long> byPriority = new LinkedHashMap<>();
        for (Priority p : Priority.values()) {
            byPriority.put(p.name(), ticketRepository.countByPriority(p));
        }

        Map<String, Long> bySeverity = new LinkedHashMap<>();
        for (Severity s : Severity.values()) {
            bySeverity.put(s.name(), ticketRepository.countBySeverity(s));
        }

        Map<String, Long> byDeveloper = new LinkedHashMap<>();
        for (User developer : userRepository.findByRoleOrderByFullNameAsc(Role.DEVELOPER)) {
            byDeveloper.put(developer.getFullName(), ticketRepository.countByAssigneeId(developer.getId()));
        }

        // percentage of tickets that are Resolved or Closed, rounded to 1 decimal place
        double resolutionRate = 0;
        if (total > 0) {
            resolutionRate = Math.round((resolved + closed) * 1000.0 / total) / 10.0;
        }

        List<TicketResponse> recent = new ArrayList<>();
        for (Ticket ticket : ticketRepository.findTop5ByOrderByCreatedAtDesc()) {
            recent.add(toResponse(ticket, viewer));
        }

        return new DashboardStats(total, open, inProgress, resolved, closed, unassigned, resolutionRate,
                byStatus, byPriority, bySeverity, byDeveloper, recent);
    }

    // --------------------------------------------------------------- HELPERS

    private Ticket findTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id " + id));
    }

    private boolean matchesAssignee(Ticket ticket, Long assigneeId) {
        if (assigneeId == null) {
            return true; // no filter
        }
        if (assigneeId == 0) {
            return ticket.getAssignee() == null; // 0 = "unassigned"
        }
        return ticket.getAssignee() != null && assigneeId.equals(ticket.getAssignee().getId());
    }

    /** True if the search text appears in the ticket number, title, description or reporter name. */
    private boolean matchesKeyword(Ticket ticket, String text) {
        String code = ticket.getTicketCode() == null ? "" : ticket.getTicketCode().toLowerCase();
        return code.contains(text)
                || ticket.getTitle().toLowerCase().contains(text)
                || ticket.getDescription().toLowerCase().contains(text)
                || ticket.getReporter().getFullName().toLowerCase().contains(text);
    }

    /** Converts a Ticket (database object) into a TicketResponse (what the browser receives). */
    private TicketResponse toResponse(Ticket ticket, User viewer) {
        RolePermissions permissions = PermissionFactory.forRole(viewer.getRole());

        List<String> allowedStatuses = new ArrayList<>();
        for (Status status : permissions.allowedStatuses(viewer, ticket)) {
            allowedStatuses.add(status.name());
        }

        UserResponse assignee = null;
        if (ticket.getAssignee() != null) {
            assignee = UserResponse.from(ticket.getAssignee());
        }

        return new TicketResponse(
                ticket.getId(),
                ticket.getTicketCode(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority().name(),
                ticket.getSeverity().name(),
                ticket.getStatus().name(),
                UserResponse.from(ticket.getReporter()),
                assignee,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                permissions.canEditTicket(viewer, ticket),
                permissions.canDeleteTicket(viewer, ticket),
                permissions.canAssignTicket(),
                allowedStatuses);
    }
}
