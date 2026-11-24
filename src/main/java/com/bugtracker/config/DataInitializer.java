package com.bugtracker.config;

import com.bugtracker.model.Comment;
import com.bugtracker.model.Priority;
import com.bugtracker.model.Role;
import com.bugtracker.model.Severity;
import com.bugtracker.model.Status;
import com.bugtracker.model.Ticket;
import com.bugtracker.model.User;
import com.bugtracker.repository.CommentRepository;
import com.bugtracker.repository.TicketRepository;
import com.bugtracker.repository.UserRepository;
import com.bugtracker.util.TicketCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Runs once every time the application starts.
 * If the database is empty it inserts the demo users, tickets and comments.
 * (If you want to reset the demo data: drop the "bugtracker" database and start the app again.)
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           TicketRepository ticketRepository,
                           CommentRepository commentRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // data already there - do nothing
        }

        // ---------------- demo users ----------------
        User admin = createUser("admin", "admin123", "Alex Morgan", "alex.morgan@bugtracker.com", Role.ADMIN);
        User dev1 = createUser("dev", "dev123", "Dev Patel", "dev.patel@bugtracker.com", Role.DEVELOPER);
        User dev2 = createUser("dev2", "dev123", "Priya Nair", "priya.nair@bugtracker.com", Role.DEVELOPER);
        User tester1 = createUser("tester", "tester123", "Tina Brooks", "tina.brooks@bugtracker.com", Role.TESTER);
        User tester2 = createUser("tester2", "tester123", "Marcus Lee", "marcus.lee@bugtracker.com", Role.TESTER);

        // ---------------- demo tickets (the last number = how many hours ago it was reported) ----------------
        Ticket t1 = createTicket("Login button does nothing on Safari",
                "On Safari 17 the Sign In button does not respond when clicked. No error is shown. "
                        + "Works fine in Chrome and Firefox.",
                Priority.HIGH, Severity.MAJOR, Status.IN_PROGRESS, tester1, dev1, 120);

        Ticket t2 = createTicket("Dashboard crashes when there are no tickets",
                "A brand new account that has no tickets sees a blank white page instead of the dashboard cards.",
                Priority.MEDIUM, Severity.MAJOR, Status.RESOLVED, tester2, dev2, 100);

        createTicket("Typo in password reset email",
                "The email says \"Reset you password\". It should say \"Reset your password\".",
                Priority.LOW, Severity.MINOR, Status.CLOSED, tester1, dev2, 200);

        Ticket t4 = createTicket("Payment fails for amounts over $1000",
                "Checkout returns error 500 whenever the cart total is higher than $1000. "
                        + "Steps: add 3 laptops to the cart, press Pay Now.",
                Priority.CRITICAL, Severity.BLOCKER, Status.OPEN, admin, dev1, 30);

        Ticket t5 = createTicket("Search ignores uppercase letters",
                "Searching for \"Invoice\" finds nothing but \"invoice\" works. Search should not care about case.",
                Priority.MEDIUM, Severity.MODERATE, Status.IN_PROGRESS, tester2, dev2, 72);

        createTicket("Profile picture upload shows a blank image",
                "After uploading a .png profile picture the preview is empty until the page is refreshed twice.",
                Priority.HIGH, Severity.MAJOR, Status.OPEN, tester1, null, 48);

        createTicket("Page layout breaks on small mobile screens",
                "On a phone (width below 400px) the navigation menu overlaps the page title.",
                Priority.MEDIUM, Severity.MODERATE, Status.OPEN, tester2, null, 40);

        createTicket("Session expires without any warning",
                "Users are logged out in the middle of filling a form and lose all their input. "
                        + "Please show a warning before the session ends.",
                Priority.HIGH, Severity.MODERATE, Status.RESOLVED, tester1, dev1, 150);

        Ticket t9 = createTicket("Export to CSV downloads an empty file",
                "The Export button on the reports page downloads a file with only the header row.",
                Priority.HIGH, Severity.MAJOR, Status.IN_PROGRESS, admin, dev2, 26);

        createTicket("Notification badge shows the wrong count",
                "The red badge shows 5 notifications but the list only contains 3.",
                Priority.LOW, Severity.MINOR, Status.OPEN, tester1, null, 20);

        createTicket("Slow page load on the reports page",
                "The reports page needs about 8 seconds to open when there are more than 500 tickets.",
                Priority.MEDIUM, Severity.MODERATE, Status.CLOSED, tester1, dev1, 300);

        createTicket("Cannot delete my own account",
                "The Delete Account button in settings is greyed out even for accounts without any data.",
                Priority.LOW, Severity.MINOR, Status.OPEN, tester2, null, 10);

        createTicket("Date picker allows past dates for deadlines",
                "When creating a task you can choose yesterday as the deadline.",
                Priority.MEDIUM, Severity.MINOR, Status.RESOLVED, tester1, dev2, 170);

        Ticket t14 = createTicket("SQL error when the title contains an apostrophe",
                "Saving a ticket called \"User's profile\" shows a database error on screen.",
                Priority.CRITICAL, Severity.MAJOR, Status.RESOLVED, tester1, dev1, 110);

        // ---------------- demo comments ----------------
        addComment(t1, dev1, "I can reproduce this on Safari 17.2. Looks like a problem with the form submit event.", 5);
        addComment(t1, tester1, "Thanks! It also happens on iPad Safari.", 7);
        addComment(t1, dev1, "Found it - Safari needs a different event listener. Working on a fix now.", 20);

        addComment(t2, dev2, "Fixed: the dashboard now shows zero values instead of crashing.", 12);
        addComment(t2, tester2, "Verified on my side. Looks good now.", 18);

        addComment(t4, admin, "This is blocking the sales team. Please look at it first thing.", 1);
        addComment(t4, dev1, "On it. The payment gateway rejects the amount because we send it as an integer.", 4);

        addComment(t5, dev2, "Will use a case-insensitive comparison in the search query.", 10);

        addComment(t9, admin, "The client needs this export for the monthly meeting.", 2);
        addComment(t9, dev2, "The loop that writes rows was running before the data was loaded. Fix in progress.", 5);

        addComment(t14, dev1, "Switched to prepared statements, apostrophes are now safe.", 15);
        addComment(t14, tester1, "Retested with several special characters. All fine.", 30);

        log.info("Demo data created: {} users, {} tickets, {} comments",
                userRepository.count(), ticketRepository.count(), commentRepository.count());
    }

    private User createUser(String username, String plainPassword, String fullName, String email, Role role) {
        // The password is hashed before it is stored.
        User user = new User(username, passwordEncoder.encode(plainPassword), fullName, email, role);
        return userRepository.save(user);
    }

    private Ticket createTicket(String title, String description, Priority priority, Severity severity,
                                Status status, User reporter, User assignee, int hoursAgo) {
        LocalDateTime created = LocalDateTime.now().minusHours(hoursAgo);

        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setPriority(priority);
        ticket.setSeverity(severity);
        ticket.setStatus(status);
        ticket.setReporter(reporter);
        ticket.setAssignee(assignee);
        ticket.setCreatedAt(created);
        ticket.setUpdatedAt(status == Status.OPEN ? created : created.plusHours(Math.min(hoursAgo, 6)));

        Ticket saved = ticketRepository.save(ticket);
        saved.setTicketCode(TicketCodeGenerator.generate(saved.getId()));
        return ticketRepository.save(saved);
    }

    private void addComment(Ticket ticket, User author, String text, int hoursAfterReport) {
        Comment comment = new Comment(ticket, author, text);
        comment.setCreatedAt(ticket.getCreatedAt().plusHours(hoursAfterReport));
        commentRepository.save(comment);
    }
}
