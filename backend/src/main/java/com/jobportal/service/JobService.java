package com.jobportal.service;

import com.jobportal.dto.CreateJobRequestDto;
import com.jobportal.dto.JobResponseDto;
import com.jobportal.model.Job;
import com.jobportal.model.User;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public JobResponseDto createJob(CreateJobRequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Job job = modelMapper.map(requestDto, Job.class);

        job.setEmployer(user);
        job.setPostedDate(LocalDateTime.now());

        Job savedJob = jobRepository.save(job);

        JobResponseDto response = modelMapper.map(savedJob, JobResponseDto.class);
        response.setPostedById(user.getId());
        response.setPostedByName(user.getName());

        return response;
    }

    public List<JobResponseDto> getAllJobs(String title, String location, String type) {
        // If a parameter comes in empty or blank from the frontend, normalize it to null
        String searchTitle = (title != null && !title.isBlank()) ? title : null;
        String searchLocation = (location != null && !location.isBlank()) ? location : null;
        String searchType = (type != null && !type.isBlank()) ? type : null;

        // Route directly into your specialized repository query mapping
        return jobRepository.searchJobs(searchTitle, searchLocation, searchType).stream()
                .map(job -> {
                    JobResponseDto dto = modelMapper.map(job, JobResponseDto.class);

                    if (job.getEmployer() != null) {
                        dto.setPostedById(job.getEmployer().getId());
                        dto.setPostedByName(job.getEmployer().getName());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public JobResponseDto getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));

        JobResponseDto response = modelMapper.map(job, JobResponseDto.class);

        if (job.getEmployer() != null) {
            response.setPostedById(job.getEmployer().getId());
            response.setPostedByName(job.getEmployer().getName());
        }
        return response;
    }

    public void deleteJob(Long id, Long userId, String userRole){
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));

        boolean isAdmin = "admin".equalsIgnoreCase(userRole);
        boolean isOwner = job.getEmployer() != null && job.getEmployer().getId().equals(userId);

        if (!isAdmin && !isOwner) throw new RuntimeException("You are not authorized to delete this job posting");

        jobRepository.delete(job);
    }
}
