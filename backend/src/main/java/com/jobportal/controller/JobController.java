package com.jobportal.controller;

import com.jobportal.dto.CreateJobRequestDto;
import com.jobportal.dto.JobResponseDto;
import com.jobportal.model.User;
import com.jobportal.service.JobService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/jobs")
// @CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"}, allowCredentials = "true")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final ModelMapper modelMapper;

    // Alternate getAllJobs
    @GetMapping
    public ResponseEntity<List<JobResponseDto>> getAllJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String type) {

        // Route parameter states downstream into your updated application service layer
        List<JobResponseDto> jobs = jobService.getAllJobs(title, location, type);
        return ResponseEntity.ok(jobs);
    }

    // Alternate addJobs
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobResponseDto> createJob(@RequestBody CreateJobRequestDto requestDto,
                                                    @AuthenticationPrincipal User user) {

        JobResponseDto responseDto = jobService.createJob(requestDto, user.getId());
        return ResponseEntity.ok(responseDto);
    }

    // Alternate getJobById
    @GetMapping("/{id}")
    public ResponseEntity<JobResponseDto> getJobById(@PathVariable Long id) {
        JobResponseDto responseDto = jobService.getJobById(id);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<?> deleteJob(@PathVariable Long id, @AuthenticationPrincipal User user) {

        try {
            // user.getRole().name() automatically provides the clean string ("EMPLOYER", "ADMIN", etc.)
            jobService.deleteJob(id, user.getId(), user.getRole().name());
            return ResponseEntity.ok(java.util.Map.of("message", "Job listing deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
