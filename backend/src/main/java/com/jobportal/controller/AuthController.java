package com.jobportal.controller;

import com.jobportal.model.User;
import com.jobportal.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

/**
 * AuthController — handles user registration, login, and logout.
 *
 * Endpoints:
 *   POST /register  — Register a new user
 *   POST /login     — Login, stores user in session
 *   POST /logout    — Invalidates the session
 *   GET  /me        — Returns currently logged-in user from session
 */
@RestController
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"}, allowCredentials = "true")
@RequiredArgsConstructor
public class AuthController {

    private UserRepository userRepository;

    // ── POST /register ────────────────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // Check if email is already taken
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }
        // Save user (password stored as plain text – fine for college project)
        User saved = userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Registered successfully", "id", saved.getId()));
    }

    // ── POST /login ───────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session) {
        String email    = body.get("email");
        String password = body.get("password");

        Optional<User> opt = userRepository.findByEmail(email);

        // Check credentials
        if (opt.isEmpty() || !opt.get().getPassword().equals(password)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }

        User user = opt.get();

        // Store user info in the HTTP session
        session.setAttribute("userId",   user.getId());
        session.setAttribute("userName", user.getName());
        session.setAttribute("userRole", user.getRole());

        // Return basic user info to the frontend (stored in localStorage)
        return ResponseEntity.ok(Map.of(
            "id",   user.getId(),
            "name", user.getName(),
            "role", user.getRole()
        ));
    }

    // ── POST /logout ──────────────────────────────────────────────────────────
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    // ── GET /me ───────────────────────────────────────────────────────────────
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }
        return ResponseEntity.ok(Map.of(
            "id",   userId,
            "name", session.getAttribute("userName"),
            "role", session.getAttribute("userRole")
        ));
    }
}
