package com.jobportal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Role {
    @JsonProperty("seeker")
    SEEKER,
    @JsonProperty("employer")
    EMPLOYER,
    @JsonProperty("admin")
    ADMIN;


    // to seamlessly map incoming lowercase strings from the database/JSON
    public static Role fromString(String role) {
        return Role.valueOf(role.toUpperCase());
    }
}
