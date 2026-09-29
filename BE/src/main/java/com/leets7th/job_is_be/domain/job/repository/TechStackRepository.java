package com.leets7th.job_is_be.domain.job.repository;

import com.leets7th.job_is_be.domain.job.entity.TechStack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TechStackRepository extends JpaRepository<TechStack, Long> {

    Optional<TechStack> findByNormalizedName(String normalizedName);
}
