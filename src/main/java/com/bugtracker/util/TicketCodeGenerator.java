package com.bugtracker.util;

/**
 * Creates the unique, human friendly ticket numbers: BUG-1001, BUG-1002, BUG-1003 ...
 * The number is based on the database id, which MySQL guarantees is unique.
 */
public class TicketCodeGenerator {

    private static final String PREFIX = "BUG-";
    private static final long START_NUMBER = 1000;

    /** Utility class - nobody should create objects of it. */
    private TicketCodeGenerator() {
    }

    public static String generate(Long databaseId) {
        return PREFIX + (START_NUMBER + databaseId);
    }
}
