package com.leets7th.job_is_be.domain.user.repository;

import com.leets7th.job_is_be.domain.user.entity.UserWithdrawal;
import com.leets7th.job_is_be.domain.user.enums.WithdrawalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserWithdrawalRepository extends JpaRepository<UserWithdrawal, Long> {

    Optional<UserWithdrawal> findFirstByUserIdAndStatusOrderByRequestedAtDesc(
            Long userId,
            WithdrawalStatus status
    );
}
