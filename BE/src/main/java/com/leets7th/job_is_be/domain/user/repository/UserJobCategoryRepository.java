package com.leets7th.job_is_be.domain.user.repository;

import com.leets7th.job_is_be.domain.user.entity.UserJobCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserJobCategoryRepository extends JpaRepository<UserJobCategory, Long> {

    List<UserJobCategory> findAllByUserId(Long userId);

    void deleteAllByUserId(Long userId);
}
