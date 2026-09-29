package com.leets7th.job_is_be.domain.notification.repository;

import com.leets7th.job_is_be.domain.notification.entity.UnsubscribeFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnsubscribeFeedbackRepository extends JpaRepository<UnsubscribeFeedback, Long> {
}
