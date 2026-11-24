package com.bugtracker.service;

import com.bugtracker.dto.LoginRequest;
import com.bugtracker.dto.UserResponse;
import com.bugtracker.exception.UnauthorizedException;
import com.bugtracker.model.User;
import com.bugtracker.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Handles login, logout and "who is the current user?". */
@Service
public class AuthService {

    /** The name under which we remember the logged-in user's id inside the HTTP session. */
    public static final String SESSION_USER_ID = "LOGGED_IN_USER_ID";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse login(LoginRequest request, HttpSession session) {
        // Same message for "unknown user" and "wrong password" so nobody can guess usernames.
        User user = userRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password.");
        }

        session.setAttribute(SESSION_USER_ID, user.getId());
        return UserResponse.from(user);
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    /** Returns the User who is logged in, or throws 401 if nobody is. */
    public User getCurrentUser(HttpSession session) {
        Object userId = session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            throw new UnauthorizedException("Please log in first.");
        }
        return userRepository.findById((Long) userId)
                .orElseThrow(() -> new UnauthorizedException("Please log in again."));
    }
}
