package com.leets7th.job_is_be.domain.job.repository;

import com.leets7th.job_is_be.domain.job.entity.JobCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface JobCategoryRepository extends JpaRepository<JobCategory, Long> {
    Optional<JobCategory> findByName(String name);
}
