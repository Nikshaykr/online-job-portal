package com.jobportal.model;

import jakarta.persistence.*;

/**
 * User entity — maps to the 'users' table.
 * Roles: 'seeker', 'employer', 'admin'
 */
@Entity
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

    // ── Constructors ──────────────────────────────────────────────────────────
    public User() {}

    public User(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public Long getId()              { return id; }
    public String getName()          { return name; }
    public String getEmail()         { return email; }
    public String getPassword()      { return password; }
    public String getRole()          { return role; }
    public String getResumePath()    { return resumePath; }

    public void setId(Long id)               { this.id = id; }
    public void setName(String name)         { this.name = name; }
    public void setEmail(String email)       { this.email = email; }
    public void setPassword(String p)        { this.password = p; }
    public void setRole(String role)         { this.role = role; }
    public void setResumePath(String path)   { this.resumePath = path; }
}
