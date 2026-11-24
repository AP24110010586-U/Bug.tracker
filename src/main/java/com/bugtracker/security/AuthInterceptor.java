package com.bugtracker.security;

import com.bugtracker.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * A "gatekeeper" that runs before every /api/** request.
 * If nobody is logged in, the request is stopped here and the browser gets a 401 message.
 */
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false); // false = do not create a new session
        boolean loggedIn = session != null && session.getAttribute(AuthService.SESSION_USER_ID) != null;

        if (!loggedIn) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"status\":401,\"message\":\"Please log in first.\"}");
            return false; // stop - the controller is never reached
        }
        return true; // carry on to the controller
    }
}
