package com.bugtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the application.
 * Run this class (or use "mvn spring-boot:run") to start the server on http://localhost:8080
 */
@SpringBootApplication
public class BugTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BugTrackerApplication.class, args);
    }
}
