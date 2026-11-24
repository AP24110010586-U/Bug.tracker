package com.bugtracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

import java.time.LocalDateTime;

/**
 * ABSTRACTION + INHERITANCE:
 * Every database table in this project needs an id and a creation time,
 * so that shared code lives here once. User, Ticket and Comment all "extend" it.
 *
 * ENCAPSULATION: the fields are private and are only reached through getters/setters.
 */
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Called automatically by JPA just before the row is first saved. */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Abstract method: every subclass MUST say how it describes itself.
     * (Used in log messages - this is POLYMORPHISM, each class answers differently.)
     */
    public abstract String describe();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
