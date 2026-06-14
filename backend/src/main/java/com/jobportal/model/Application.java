package com.jobportal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Application entity — maps to the 'applications' table.
 * Represents a jobseeker applying to a job.
 */
@Entity
// can use lombok @Getter, @Setter, @NoArgsConstructor & @AllArgsConstructor annotations here for removing repetitive code
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}
