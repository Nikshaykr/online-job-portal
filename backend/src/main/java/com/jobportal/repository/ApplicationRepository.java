package com.jobportal.repository;

import com.jobportal.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByJobIdAndSeekerId(Long jobId, Long seekerId);

    List<Application> findBySeekerId(Long seekerId);

    List<Application> findByJobEmployerId(Long employerId);

    // True if the given seeker has applied to at least one job owned by the given employer.
    // Backs the ownership guardrail on resume downloads.
    boolean existsByJobEmployerIdAndSeekerId(Long employerId, Long seekerId);
}
