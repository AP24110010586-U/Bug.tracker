package com.bugtracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** A comment written on a ticket. Stored in the "comments" table. */
@Entity
@Table(name = "comments")
public class Comment extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 2000)
    private String content;

    public Comment() {
    }

    public Comment(Ticket ticket, User author, String content) {
        this.ticket = ticket;
        this.author = author;
        this.content = content;
    }

    @Override
    public String describe() {
        return "Comment by " + author.getFullName() + " on " + ticket.getTicketCode();
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
