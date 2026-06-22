package com.jobportal.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobResponseDto {
    private Long id;
    private String title;
    private String description;
    private String company;
    private String location;
    private String type;
    private Long postedById;      // Instead of exposing the full internal User model, we just send their ID
    private String postedByName;  // Clean layout to display the publisher's name directly on the frontend
    private LocalDateTime postedDate;
}
