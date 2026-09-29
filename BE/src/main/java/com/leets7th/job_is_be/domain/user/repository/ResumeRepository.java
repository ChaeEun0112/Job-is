package com.leets7th.job_is_be.domain.user.repository;

import com.leets7th.job_is_be.domain.user.entity.Resume;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.enums.ResumeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    Optional<Resume> findByUserAndCategory(User user, ResumeCategory category);

    List<Resume> findAllByUser(User user);

    Optional<Resume> findByIdAndUser(Long id, User user);
}
