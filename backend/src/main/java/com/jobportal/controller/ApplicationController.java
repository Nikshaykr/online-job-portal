package com.jobportal.controller;

import com.jobportal.dto.ApplicationResponseDto;
import com.jobportal.dto.ApplyJobRequestDto;
import com.jobportal.dto.UpdateStatusRequestDto;
import com.jobportal.service.ApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"}, allowCredentials = "true")
@RequiredArgsConstructor
public class ApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    private final ApplicationService applicationService;

    // ── POST /applications (Seekers apply for a job) ──────────────────────────
    @PostMapping("/applications")
    @PreAuthorize("hasRole('SEEKER')")
    public ResponseEntity<?> applyToJob(@RequestBody ApplyJobRequestDto requestDto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null){
            return ResponseEntity.status(401).build();
        }

        try {
            ApplicationResponseDto responseDto = applicationService.applyToJob(requestDto, userId);
            return ResponseEntity.ok(responseDto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET /applications (Fetch list depending on logged-in Role) ─────────────
    @GetMapping("/applications")
    @PreAuthorize("hasAnyRole('SEEKER', 'EMPLOYER')")
    public ResponseEntity<List<ApplicationResponseDto>> getApplications(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("userRole");

        if (userId == null) return ResponseEntity.status(401).build();

        List<ApplicationResponseDto> applications = applicationService.getApplicationsForUser(userId, userRole);
        return ResponseEntity.ok(applications);
    }

    // ── POST /applications/update-status (Employer accepts/rejects) ────────────
    @PostMapping("/applications/update-status")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<?> updateStatus(@RequestBody UpdateStatusRequestDto requestDto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();

        try {
            applicationService.updateApplicationStatus(requestDto, userId);
            return ResponseEntity.ok(Map.of("message", "Application status updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }
}