package com.bugtracker.repository;

import com.bugtracker.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** All comments of one ticket, oldest first. */
    List<Comment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    /** Used when a ticket is deleted, so no orphan comments stay behind. */
    void deleteByTicketId(Long ticketId);
}
