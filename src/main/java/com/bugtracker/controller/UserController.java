package com.bugtracker.controller;

import com.bugtracker.dto.UserResponse;
import com.bugtracker.model.Role;
import com.bugtracker.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** GET /api/users            -> everybody
 *  GET /api/users?role=DEVELOPER  -> only developers (used by the "assign" drop-downs) */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> list(@RequestParam(name = "role", required = false) Role role) {
        return userService.listUsers(role);
    }
}
