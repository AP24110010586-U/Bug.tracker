-- =====================================================================
--  Bug & Ticket Tracking System - database reference script
--
--  YOU DO NOT NEED TO RUN THIS FILE.
--  When the Spring Boot app starts it creates the database "bugtracker" and all
--  tables by itself (spring.jpa.hibernate.ddl-auto=update) and inserts the demo data.
--
--  This file is here so you can SEE the table design (useful for your report / viva)
--  and try the example queries at the bottom in MySQL Workbench or the mysql shell.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS bugtracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bugtracker;

-- ---------------------------------------------------------------- users
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    created_at  DATETIME(6)  NOT NULL,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(255) NOT NULL,                 -- BCrypt hash, never plain text
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL,
    role        ENUM('ADMIN','DEVELOPER','TESTER') NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------- tickets
CREATE TABLE IF NOT EXISTS tickets (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    created_at   DATETIME(6)   NOT NULL,
    ticket_code  VARCHAR(20)   NULL,                   -- BUG-1001, BUG-1002 ...
    title        VARCHAR(200)  NOT NULL,
    description  VARCHAR(4000) NOT NULL,
    priority     ENUM('LOW','MEDIUM','HIGH','CRITICAL')       NOT NULL,
    severity     ENUM('MINOR','MODERATE','MAJOR','BLOCKER')   NOT NULL,
    status       ENUM('OPEN','IN_PROGRESS','RESOLVED','CLOSED') NOT NULL,
    reporter_id  BIGINT        NOT NULL,
    assignee_id  BIGINT        NULL,
    updated_at   DATETIME(6)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tickets_code (ticket_code),
    CONSTRAINT fk_tickets_reporter FOREIGN KEY (reporter_id) REFERENCES users (id),
    CONSTRAINT fk_tickets_assignee FOREIGN KEY (assignee_id) REFERENCES users (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------- comments
CREATE TABLE IF NOT EXISTS comments (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    created_at  DATETIME(6)   NOT NULL,
    content     VARCHAR(2000) NOT NULL,
    ticket_id   BIGINT        NOT NULL,
    author_id   BIGINT        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_comments_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id),
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users (id)
) ENGINE = InnoDB;

-- =====================================================================
--  Example queries (run them after the app has started once)
-- =====================================================================

-- Number of tickets per status
-- SELECT status, COUNT(*) AS total FROM tickets GROUP BY status;

-- All open tickets with reporter and developer names
-- SELECT t.ticket_code, t.title, t.priority, r.full_name AS reporter, a.full_name AS assignee
-- FROM tickets t
-- JOIN users r      ON r.id = t.reporter_id
-- LEFT JOIN users a ON a.id = t.assignee_id
-- WHERE t.status = 'OPEN';

-- Workload per developer
-- SELECT u.full_name, COUNT(t.id) AS assigned_tickets
-- FROM users u LEFT JOIN tickets t ON t.assignee_id = u.id
-- WHERE u.role = 'DEVELOPER' GROUP BY u.full_name;
