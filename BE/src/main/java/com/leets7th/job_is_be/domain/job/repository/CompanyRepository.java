package com.leets7th.job_is_be.domain.job.repository;

import com.leets7th.job_is_be.domain.job.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByNormalizedName(String normalizedName);
}
