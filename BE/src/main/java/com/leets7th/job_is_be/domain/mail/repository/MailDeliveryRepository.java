package com.leets7th.job_is_be.domain.mail.repository;

import com.leets7th.job_is_be.domain.mail.entity.MailDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MailDeliveryRepository extends JpaRepository<MailDelivery, Long> {

    Optional<MailDelivery> findByDeliveryKey(String deliveryKey);
}
