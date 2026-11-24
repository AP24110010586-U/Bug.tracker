package com.bugtracker.model;

/** How badly the bug hurts the software (technical impact). */
public enum Severity {
    MINOR("Minor"),
    MODERATE("Moderate"),
    MAJOR("Major"),
    BLOCKER("Blocker");

    private final String label;

    Severity(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
