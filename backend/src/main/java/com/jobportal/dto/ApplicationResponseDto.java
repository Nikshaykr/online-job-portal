package com.jobportal.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ApplicationResponseDto {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String company;

    private Long seekerId;
    private String seekerName;
    private String seekerEmail;

    private String status;
    private LocalDate appliedDate;
}