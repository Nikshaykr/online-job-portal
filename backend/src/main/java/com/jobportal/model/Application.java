package com.jobportal.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Application entity — maps to the 'applications' table.
 * Represents a job seeker applying to a job.
 */
@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key: id of the seeker (User) who applied
    @Column(nullable = false)
    private Long seekerId;

    // Foreign key: id of the job applied to
    @Column(nullable = false)
    private Long jobId;

    // Status: Applied, Shortlisted, Rejected
    @Column(nullable = false)
    private String status;

    // Date when the application was submitted
    private LocalDate appliedDate;

    // ── Constructors ──────────────────────────────────────────────────────────
    public Application() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public Long getId()               { return id; }
    public Long getSeekerId()         { return seekerId; }
    public Long getJobId()            { return jobId; }
    public String getStatus()         { return status; }
    public LocalDate getAppliedDate() { return appliedDate; }

    public void setId(Long id)                  { this.id = id; }
    public void setSeekerId(Long seekerId)      { this.seekerId = seekerId; }
    public void setJobId(Long jobId)            { this.jobId = jobId; }
    public void setStatus(String status)        { this.status = status; }
    public void setAppliedDate(LocalDate date)  { this.appliedDate = date; }
}
