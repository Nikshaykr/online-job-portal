package com.jobportal.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Job entity — maps to the 'jobs' table.
 * Created by employers.
 */
@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String location;

    // e.g. Full-time, Part-time, Internship, Remote
    @Column(nullable = false)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Foreign key: id of the employer (User) who posted this job
    @Column(nullable = false)
    private Long employerId;

    // Date when the job was posted
    private LocalDate postedDate;

    // ── Constructors ──────────────────────────────────────────────────────────
    public Job() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public Long getId()               { return id; }
    public String getTitle()          { return title; }
    public String getCompany()        { return company; }
    public String getLocation()       { return location; }
    public String getType()           { return type; }
    public String getDescription()    { return description; }
    public Long getEmployerId()       { return employerId; }
    public LocalDate getPostedDate()  { return postedDate; }

    public void setId(Long id)                    { this.id = id; }
    public void setTitle(String title)            { this.title = title; }
    public void setCompany(String company)        { this.company = company; }
    public void setLocation(String location)      { this.location = location; }
    public void setType(String type)              { this.type = type; }
    public void setDescription(String d)          { this.description = d; }
    public void setEmployerId(Long employerId)    { this.employerId = employerId; }
    public void setPostedDate(LocalDate date)     { this.postedDate = date; }
}
