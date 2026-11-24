package com.bugtracker.model;

/** The three kinds of users that can log in. */
public enum Role {
    ADMIN("Admin"),
    DEVELOPER("Developer"),
    TESTER("Tester");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
