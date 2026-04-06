package com.jobportal.controller;

import com.jobportal.model.Job;
import com.jobportal.repository.JobRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JobController — CRUD operations for job postings.
 *
 * Endpoints:
 *   GET    /jobs          — Search/list all jobs (query params: title, location, type)
 *   GET    /jobs/{id}     — Get a single job
 *   POST   /jobs/add      — Post a new job (employer only)
 *   DELETE /jobs/{id}     — Delete a job (admin or job owner)
 */
@RestController
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"}, allowCredentials = "true")
public class JobController {

    @Autowired
    private JobRepository jobRepository;

    // ── GET /jobs ─────────────────────────────────────────────────────────────
    @GetMapping("/jobs")
    public ResponseEntity<?> getJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String type) {

        // If no filters, return all jobs; otherwise run the search query
        List<Job> jobs;
        if (title == null && location == null && type == null) {
            jobs = jobRepository.findAll();
        } else {
            jobs = jobRepository.searchJobs(
                    (title    != null && title.isBlank())    ? null : title,
                    (location != null && location.isBlank()) ? null : location,
                    (type     != null && type.isBlank())     ? null : type
            );
        }
        return ResponseEntity.ok(jobs);
    }

    // ── GET /jobs/{id} ────────────────────────────────────────────────────────
    @GetMapping("/jobs/{id}")
    public ResponseEntity<?> getJob(@PathVariable Long id) {
        Optional<Job> opt = jobRepository.findById(id);
        return opt.<ResponseEntity<?>>map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    // ── POST /jobs/add ────────────────────────────────────────────────────────
    @PostMapping("/jobs/add")
    public ResponseEntity<?> addJob(
            @RequestBody Job job,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestHeader(value = "X-User-Role", required = false) String headerRole,
            HttpSession session) {
        
        // Use headers if present, fallback to session
        Long userId = headerUserId != null ? headerUserId : (Long) session.getAttribute("userId");
        String role = headerRole != null ? headerRole : (String) session.getAttribute("userRole");

        // Only employers can post jobs
        if (userId == null || !"employer".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only employers can post jobs"));
        }

        job.setEmployerId(userId);
        job.setPostedDate(LocalDate.now());
        Job saved = jobRepository.save(job);
        return ResponseEntity.ok(saved);
    }

    // ── DELETE /jobs/{id} ─────────────────────────────────────────────────────
    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Long id, HttpSession session) {
        String role = (String) session.getAttribute("userRole");

        // Only admins can delete any job
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        if (!jobRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        jobRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Job deleted"));
    }
}
