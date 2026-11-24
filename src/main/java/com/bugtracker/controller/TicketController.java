package com.bugtracker.controller;

import com.bugtracker.dto.AssignRequest;
import com.bugtracker.dto.StatusRequest;
import com.bugtracker.dto.TicketRequest;
import com.bugtracker.dto.TicketResponse;
import com.bugtracker.model.Priority;
import com.bugtracker.model.Severity;
import com.bugtracker.model.Status;
import com.bugtracker.model.User;
import com.bugtracker.service.AuthService;
import com.bugtracker.service.TicketService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST API for tickets. The controller is thin: it reads the request,
 * asks the service to do the work and returns the result as JSON.
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService; // we depend on the INTERFACE, not on the implementation
    private final AuthService authService;

    public TicketController(TicketService ticketService, AuthService authService) {
        this.ticketService = ticketService;
        this.authService = authService;
    }

    /** GET /api/tickets?search=login&status=OPEN&priority=HIGH&severity=MAJOR&assigneeId=2 */
    @GetMapping
    public List<TicketResponse> list(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "status", required = false) Status status,
            @RequestParam(name = "priority", required = false) Priority priority,
            @RequestParam(name = "severity", required = false) Severity severity,
            @RequestParam(name = "assigneeId", required = false) Long assigneeId,
            HttpSession session) {
        User user = authService.getCurrentUser(session);
        return ticketService.search(user, search, status, priority, severity, assigneeId);
    }

    @GetMapping("/{id}")
    public TicketResponse get(@PathVariable("id") Long id, HttpSession session) {
        User user = authService.getCurrentUser(session);
        return ticketService.getById(user, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse create(@Valid @RequestBody TicketRequest request, HttpSession session) {
        User user = authService.getCurrentUser(session);
        return ticketService.create(user, request);
    }

    @PutMapping("/{id}")
    public TicketResponse update(@PathVariable("id") Long id,
                                 @Valid @RequestBody TicketRequest request,
                                 HttpSession session) {
        User user = authService.getCurrentUser(session);
        return ticketService.update(user, id, request);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable("id") Long id, HttpSession session) {
        User user = authService.getCurrentUser(session);
        ticketService.delete(user, id);
        return Map.of("message", "Ticket deleted");
    }

    @PutMapping("/{id}/status")
    public TicketResponse changeStatus(@PathVariable("id") Long id,
                                       @Valid @RequestBody StatusRequest request,
                                       HttpSession session) {
        User user = authService.getCurrentUser(session);
        return ticketService.changeStatus(user, id, request.status());
    }

    @PutMapping("/{id}/assign")
    public TicketResponse assign(@PathVariable("id") Long id,
                                 @RequestBody AssignRequest request,
                                 HttpSession session) {
        User user = authService.getCurrentUser(session);
        return ticketService.assign(user, id, request.assigneeId());
    }
}
