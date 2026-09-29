package com.leets7th.job_is_be.domain.user.repository;

import com.leets7th.job_is_be.domain.user.entity.UserConsent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserConsentRepository extends JpaRepository<UserConsent, Long> {

    Optional<UserConsent> findByUserId(Long userId);
}
