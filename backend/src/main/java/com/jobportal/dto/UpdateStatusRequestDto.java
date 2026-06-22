package com.jobportal.dto;

import lombok.Data;

@Data
public class UpdateStatusRequestDto {
    private Long applicationId;
    private String status;
}
