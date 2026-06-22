package com.jobportal.dto;

import com.jobportal.model.Role;
import lombok.Data;

@Data
public class SignUpResponseDto {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private String message;
}
