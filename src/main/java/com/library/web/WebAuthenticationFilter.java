package com.library.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class WebAuthenticationFilter extends OncePerRequestFilter {
    static final String ADMIN_ID = "libraryAdminId";
    static final String ADMIN_USERNAME = "libraryAdminUsername";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith(request.getContextPath() + "/api/")
                || path.startsWith(request.getContextPath() + "/api/auth/")
                || path.equals(request.getContextPath() + "/api/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(ADMIN_ID) == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\":\"Please sign in to continue.\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
