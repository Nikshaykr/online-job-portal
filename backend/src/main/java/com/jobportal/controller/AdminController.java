package com.jobportal.controller;

import com.jobportal.dto.JobResponseDto;
import com.jobportal.dto.UserResponseDto;
import com.jobportal.model.Job;
import com.jobportal.model.User;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"}, allowCredentials = "true")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final JobRepository  jobRepository;
    private final ModelMapper modelMapper;

    // ── GET /admin/users ──────────────────────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDto>> listUsers() {
        List<UserResponseDto> users = userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserResponseDto.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    // ── GET /admin/jobs ───────────────────────────────────────────────────────
    @GetMapping("/jobs")
    public ResponseEntity<List<JobResponseDto>> listJobs() {
        List<JobResponseDto> jobs = jobRepository.findAll().stream()
                .map(job -> {
                    JobResponseDto dto = modelMapper.map(job, JobResponseDto.class);
                    if (job.getEmployer() != null) {
                        dto.setPostedById(job.getEmployer().getId());
                        dto.setPostedByName(job.getEmployer().getName());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(jobs);
    }

    // ── DELETE /admin/jobs/{id} ───────────────────────────────────────────────
    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Long id) {
        if (!jobRepository.existsById(id))
            return ResponseEntity.notFound().build();

        jobRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Job " + id + "deleted successfully by Admin"));
    }
}