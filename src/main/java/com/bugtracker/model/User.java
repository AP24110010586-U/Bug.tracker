package com.bugtracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** A person who can log in: an Admin, a Developer or a Tester. Stored in the "users" table. */
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** Stored as a BCrypt hash, never as plain text. */
    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /** JPA needs an empty constructor. */
    public User() {
    }

    public User(String username, String password, String fullName, String email, Role role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    @Override
    public String describe() {
        return fullName + " (" + role.getLabel() + ")";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
