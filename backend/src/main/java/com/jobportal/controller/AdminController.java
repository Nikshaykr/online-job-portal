package com.jobportal.controller;

import com.jobportal.model.Job;
import com.jobportal.model.User;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"}, allowCredentials = "true")
@RequiredArgsConstructor
public class AdminController {

    private UserRepository userRepository;
    private JobRepository  jobRepository;

    // Helper: check if admin via header or session
    private boolean isAdmin(HttpServletRequest request, HttpSession session) {
        String headerRole = request.getHeader("X-User-Role");
        if (headerRole != null) return "admin".equals(headerRole);
        return "admin".equals(session.getAttribute("userRole"));
    }

    // ── GET /admin/users ──────────────────────────────────────────────────────
    @GetMapping("/admin/users")
    public ResponseEntity<?> listUsers(HttpSession session, HttpServletRequest request) {
        if (!isAdmin(request, session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    // ── GET /admin/jobs ───────────────────────────────────────────────────────
    @GetMapping("/admin/jobs")
    public ResponseEntity<?> listJobs(HttpSession session, HttpServletRequest request) {
        if (!isAdmin(request, session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }
        List<Job> jobs = jobRepository.findAll();
        return ResponseEntity.ok(jobs);
    }

    // ── DELETE /admin/jobs/{id} ───────────────────────────────────────────────
    @DeleteMapping("/admin/jobs/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Long id,
                                       HttpSession session,
                                       HttpServletRequest request) {
        if (!isAdmin(request, session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }
        if (!jobRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        jobRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Job " + id + " deleted"));
    }
}