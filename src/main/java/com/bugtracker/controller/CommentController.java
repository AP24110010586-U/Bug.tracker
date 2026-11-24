package com.bugtracker.controller;

import com.bugtracker.dto.CommentRequest;
import com.bugtracker.dto.CommentResponse;
import com.bugtracker.model.User;
import com.bugtracker.service.AuthService;
import com.bugtracker.service.CommentService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** URLs:  GET/POST /api/tickets/{ticketId}/comments */
@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final AuthService authService;

    public CommentController(CommentService commentService, AuthService authService) {
        this.commentService = commentService;
        this.authService = authService;
    }

    @GetMapping
    public List<CommentResponse> list(@PathVariable("ticketId") Long ticketId) {
        return commentService.listComments(ticketId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse add(@PathVariable("ticketId") Long ticketId,
                               @Valid @RequestBody CommentRequest request,
                               HttpSession session) {
        User user = authService.getCurrentUser(session);
        return commentService.addComment(user, ticketId, request);
    }
}
