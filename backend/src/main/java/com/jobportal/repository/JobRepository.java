package com.jobportal.repository;

import com.jobportal.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for Job entity.
 * Custom queries support the job search feature.
 */
@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    // Returns all jobs posted by a specific employer
    List<Job> findByEmployerId(Long employerId);

    // Search jobs by title OR location (case-insensitive, partial match)
    @Query("SELECT j FROM Job j WHERE " +
           "(:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:type IS NULL OR j.type = :type)")
    List<Job> searchJobs(@Param("title") String title,
                         @Param("location") String location,
                         @Param("type") String type);
}
