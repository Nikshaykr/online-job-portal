package com.jobportal.controller;

import com.jobportal.model.Application;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"}, allowCredentials = "true")
// can use @RequiredArgsConstructor for automatic dependency injection instead of using @Autowired multiple times
@RequiredArgsConstructor
public class ApplicationController {

    private ApplicationRepository applicationRepository;
    private JobRepository         jobRepository;
    private UserRepository        userRepository;

    // Helper: get userId from header or session
    private Long resolveUserId(HttpServletRequest request, HttpSession session) {
        String h = request.getHeader("X-User-Id");
        if (h != null) try { return Long.valueOf(h); } catch (Exception ignored) {}
        return (Long) session.getAttribute("userId");
    }

    // Helper: get role from header or session
    private String resolveRole(HttpServletRequest request, HttpSession session) {
        String h = request.getHeader("X-User-Role");
        if (h != null) return h;
        return (String) session.getAttribute("userRole");
    }

    // ── POST /apply ───────────────────────────────────────────────────────────
    @PostMapping("/apply")
    public ResponseEntity<?> apply(@RequestBody Map<String, Long> body,
                                   HttpSession session,
                                   HttpServletRequest request) {
        Long userId = resolveUserId(request, session);
        String role = resolveRole(request, session);

        if (userId == null || !"seeker".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only seekers can apply"));
        }

        Long jobId = body.get("jobId");

        if (applicationRepository.existsBySeekerIdAndJobId(userId, jobId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Already applied to this job"));
        }

        Application app = new Application();
        app.setSeekerId(userId);
        app.setJobId(jobId);
        app.setStatus("Applied");
        app.setAppliedDate(LocalDate.now());

        return ResponseEntity.ok(applicationRepository.save(app));
    }

    // ── GET /my-applications ──────────────────────────────────────────────────
    @GetMapping("/my-applications")
    public ResponseEntity<?> myApplications(HttpSession session,
                                            HttpServletRequest request) {
        Long userId = resolveUserId(request, session);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }

        List<Application> apps = applicationRepository.findBySeekerId(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Application app : apps) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id",          app.getId());
            item.put("jobId",       app.getJobId());
            item.put("status",      app.getStatus());
            item.put("appliedDate", app.getAppliedDate());

            jobRepository.findById(app.getJobId()).ifPresent(job -> {
                item.put("jobTitle", job.getTitle());
                item.put("company",  job.getCompany());
                item.put("location", job.getLocation());
            });
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    // ── GET /job-applicants/{jobId} ───────────────────────────────────────────
    @GetMapping("/job-applicants/{jobId}")
    public ResponseEntity<?> jobApplicants(@PathVariable Long jobId,
                                           HttpSession session,
                                           HttpServletRequest request) {
        String role = resolveRole(request, session);
        if (!"employer".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Employer access required"));
        }

        List<Application> apps = applicationRepository.findByJobId(jobId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Application app : apps) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("applicationId", app.getId());
            item.put("seekerId",      app.getSeekerId());
            item.put("status",        app.getStatus());
            item.put("appliedDate",   app.getAppliedDate());

            userRepository.findById(app.getSeekerId()).ifPresent(user -> {
                item.put("seekerName",  user.getName());
                item.put("seekerEmail", user.getEmail());
            });
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    // ── POST /update-status ───────────────────────────────────────────────────
    @PostMapping("/update-status")
    public ResponseEntity<?> updateStatus(@RequestBody Map<String, Object> body,
                                          HttpSession session,
                                          HttpServletRequest request) {
        String role = resolveRole(request, session);
        if (!"employer".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Employer access required"));
        }

        Long appId      = Long.valueOf(body.get("applicationId").toString());
        String newStatus = body.get("status").toString();

        Optional<Application> opt = applicationRepository.findById(appId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Application app = opt.get();
        app.setStatus(newStatus);
        applicationRepository.save(app);
        return ResponseEntity.ok(Map.of("message", "Status updated to " + newStatus));
    }
}