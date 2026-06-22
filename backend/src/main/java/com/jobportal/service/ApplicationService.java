package com.jobportal.service;

import com.jobportal.dto.ApplicationResponseDto;
import com.jobportal.dto.ApplyJobRequestDto;
import com.jobportal.dto.UpdateStatusRequestDto;
import com.jobportal.model.Application;
import com.jobportal.model.Job;
import com.jobportal.model.User;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public ApplicationResponseDto applyToJob(ApplyJobRequestDto requestDto, Long seekerId) {
        Job job = jobRepository.findById(requestDto.getJobId())
                .orElseThrow(() -> new RuntimeException("Job posting not found"));

        User seeker = userRepository.findById(seekerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Clean custom repository check (We'll add this method next)
        boolean alreadyApplied = applicationRepository.existsByJobIdAndSeekerId(job.getId(), seekerId);
        if (alreadyApplied) {
            throw new RuntimeException("You have already applied to this job posting");
        }

        Application application = new Application();
        application.setJob(job);
        application.setSeeker(seeker);
        application.setStatus("PENDING");
        application.setAppliedDate(LocalDate.now());

        Application savedApp = applicationRepository.save(application);
        return mapToResponseDto(savedApp);
    }

    public List<ApplicationResponseDto> getApplicationsForUser(Long userId, String role) {
        List<Application> rawApplications;

        if ("EMPLOYER".equalsIgnoreCase(role)) {
            // Native database query filtering via job ownership
            rawApplications = applicationRepository.findByJobEmployerId(userId);
        } else {
            // Native database query filtering via seeker identity
            rawApplications = applicationRepository.findBySeekerId(userId);
        }

        return rawApplications.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public void updateApplicationStatus(UpdateStatusRequestDto requestDto, Long employerId) {
        Application app = applicationRepository.findById(requestDto.getApplicationId())
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // Direct object graph navigation: app -> job -> employer -> id
        if (!app.getJob().getEmployer().getId().equals(employerId)) {
            throw new RuntimeException("You are not authorized to alter applications for this job posting");
        }

        app.setStatus(requestDto.getStatus().toUpperCase());
        applicationRepository.save(app);
    }

    private ApplicationResponseDto mapToResponseDto(Application app) {
        ApplicationResponseDto dto = modelMapper.map(app, ApplicationResponseDto.class);

        // No manual repository cross-queries needed anymore!
        // Data streams cleanly straight through the active entity graph.
        dto.setJobId(app.getJob().getId());
        dto.setJobTitle(app.getJob().getTitle());
        dto.setCompany(app.getJob().getCompany());

        dto.setSeekerId(app.getSeeker().getId());
        dto.setSeekerName(app.getSeeker().getName());
        dto.setSeekerEmail(app.getSeeker().getEmail());

        return dto;
    }
}
