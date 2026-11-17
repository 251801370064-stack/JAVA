package com.library.web;

import com.library.model.Admin;
import com.library.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @GetMapping("/status")
    public Map<String, Boolean> status() throws Exception {
        return Map.of("setupRequired", !authService.hasAdministrator());
    }

    @GetMapping("/me")
    public Map<String, String> current(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(WebAuthenticationFilter.ADMIN_ID) == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in.");
        }
        return Map.of("username", (String) session.getAttribute(WebAuthenticationFilter.ADMIN_USERNAME));
    }

    @PostMapping("/setup")
    public Map<String, String> setup(@RequestBody SetupRequest body, HttpServletRequest request) throws Exception {
        char[] password = chars(body.password());
        char[] confirmation = chars(body.confirmation());
        try {
            authService.createFirstAdministrator(body.username(), password, confirmation);
            Admin admin = authService.authenticate(body.username(), password);
            if (admin == null) throw new IllegalStateException("Administrator was created but could not be authenticated.");
            authenticateSession(request, admin);
            return Map.of("username", admin.username());
        } finally {
            java.util.Arrays.fill(password, '\0');
            java.util.Arrays.fill(confirmation, '\0');
        }
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest body, HttpServletRequest request) throws Exception {
        char[] password = chars(body.password());
        try {
            Admin admin = authService.authenticate(body.username(), password);
            if (admin == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password is not correct.");
            authenticateSession(request, admin);
            return Map.of("username", admin.username());
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return Map.of("status", "signed out");
    }

    private void authenticateSession(HttpServletRequest request, Admin admin) {
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute(WebAuthenticationFilter.ADMIN_ID, admin.id());
        session.setAttribute(WebAuthenticationFilter.ADMIN_USERNAME, admin.username());
    }

    private char[] chars(String value) { return value == null ? new char[0] : value.toCharArray(); }

    public record LoginRequest(String username, String password) { }
    public record SetupRequest(String username, String password, String confirmation) { }
}
