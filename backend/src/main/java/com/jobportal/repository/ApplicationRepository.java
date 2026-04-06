package com.jobportal.repository;

import com.jobportal.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for Application entity.
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // All applications submitted by a specific seeker
    List<Application> findBySeekerId(Long seekerId);

    // All applications for a specific job (used by employers)
    List<Application> findByJobId(Long jobId);

    // Check if a seeker already applied to a job (avoid duplicates)
    boolean existsBySeekerIdAndJobId(Long seekerId, Long jobId);
}
