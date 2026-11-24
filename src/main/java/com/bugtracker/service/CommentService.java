package com.bugtracker.service;

import com.bugtracker.dto.CommentRequest;
import com.bugtracker.dto.CommentResponse;
import com.bugtracker.exception.ResourceNotFoundException;
import com.bugtracker.model.Comment;
import com.bugtracker.model.Ticket;
import com.bugtracker.model.User;
import com.bugtracker.repository.CommentRepository;
import com.bugtracker.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Reading and writing the comments of a ticket. Everybody who is logged in may comment. */
@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;

    public CommentService(CommentRepository commentRepository, TicketRepository ticketRepository) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> listComments(Long ticketId) {
        findTicket(ticketId); // throws 404 if the ticket does not exist

        List<CommentResponse> result = new ArrayList<>();
        for (Comment comment : commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)) {
            result.add(CommentResponse.from(comment));
        }
        return result;
    }

    public CommentResponse addComment(User author, Long ticketId, CommentRequest request) {
        Ticket ticket = findTicket(ticketId);

        Comment comment = new Comment(ticket, author, request.content().trim());
        Comment saved = commentRepository.save(comment);

        // A new comment counts as activity on the ticket.
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        return CommentResponse.from(saved);
    }

    private Ticket findTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id " + ticketId));
    }
}
