package com.jobportal.dto;

import com.jobportal.model.Role;
import lombok.Data;

@Data
public class SignUpRequestDto {
    private String name;
    private String email;
    private String password;
    private Role role;
}
