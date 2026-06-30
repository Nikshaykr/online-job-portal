package com.jobportal.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(
        name = "applications",
        // Enforce one application per (job, seeker) pair at the database level,
        // backing the service-layer duplicate check as defence in depth.
        uniqueConstraints = @UniqueConstraint(
                name = "uq_application_job_seeker",
                columnNames = {"job_id", "seeker_id"}
        )
)
@Data
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job; // Replaced Long jobId

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seeker_id", nullable = false)
    private User seeker; // Replaced Long seekerId

    private String status; // Applied (default), Shortlisted, Rejected
    private LocalDate appliedDate;
}
