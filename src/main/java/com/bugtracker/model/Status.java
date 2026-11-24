package com.bugtracker.model;

/** Where a ticket is in its life cycle. */
public enum Status {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    CLOSED("Closed");

    private final String label;

    Status(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
