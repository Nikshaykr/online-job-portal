package com.jobportal.dto;

import lombok.Data;

@Data
public class CreateJobRequestDto {
    private String title;
    private String description;
    private String company;
    private String location;
    private String type;
    private Double salary;
}
