package com.bugtracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** A bug ticket. Stored in the "tickets" table. */
@Entity
@Table(name = "tickets")
public class Ticket extends BaseEntity {

    /** Human friendly id such as BUG-1001 (generated automatically, see TicketCodeGenerator). */
    @Column(name = "ticket_code", unique = true, length = 20)
    private String ticketCode;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    /** Many tickets can be reported by one user. */
    @ManyToOne
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    /** Many tickets can be assigned to one developer (may be empty = unassigned). */
    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Ticket() {
    }

    @PrePersist
    protected void onPersist() {
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @Override
    public String describe() {
        return ticketCode + " - " + title;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
