package com.jobportal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User entity — maps to the 'users' table.
 * Roles: 'seeker', 'employer', 'admin'
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    // Role can be: seeker, employer, admin
    @Column(nullable = false)
    private String role;

    // Path to the uploaded resume file stored on the server (e.g. uploads/resume_5.pdf)
    // nullable = true means this column can be empty (user hasn't uploaded yet)
    @Column(nullable = true)
    private String resumePath;
}
