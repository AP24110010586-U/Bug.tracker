package com.bugtracker.controller;

import com.bugtracker.dto.LoginRequest;
import com.bugtracker.dto.UserResponse;
import com.bugtracker.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** URLs:  POST /api/auth/login   POST /api/auth/logout   GET /api/auth/me */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        return authService.login(request, session);
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpSession session) {
        authService.logout(session);
        return Map.of("message", "Logged out");
    }

    /** Tells the browser who is logged in (used by every page when it loads). */
    @GetMapping("/me")
    public UserResponse me(HttpSession session) {
        return UserResponse.from(authService.getCurrentUser(session));
    }
}
