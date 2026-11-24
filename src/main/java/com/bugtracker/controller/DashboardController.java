package com.bugtracker.controller;

import com.bugtracker.dto.DashboardStats;
import com.bugtracker.model.User;
import com.bugtracker.service.AuthService;
import com.bugtracker.service.TicketService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** GET /api/dashboard/stats - numbers for the Dashboard and Reports pages. */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final TicketService ticketService;
    private final AuthService authService;

    public DashboardController(TicketService ticketService, AuthService authService) {
        this.ticketService = ticketService;
        this.authService = authService;
    }

    @GetMapping("/stats")
    public DashboardStats stats(HttpSession session) {
        User user = authService.getCurrentUser(session);
        return ticketService.getStats(user);
    }
}
