package com.leets7th.job_is_be.domain.user.repository;

import com.leets7th.job_is_be.domain.user.entity.UserTechStack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTechStackRepository extends JpaRepository<UserTechStack, Long> {

    List<UserTechStack> findAllByUserIdOrderByIdAsc(Long userId);

    void deleteAllByUserId(Long userId);
}
