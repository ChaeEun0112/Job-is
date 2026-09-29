package com.leets7th.job_is_be.domain.personality.repository;

import com.leets7th.job_is_be.domain.personality.entity.PersonalityTest;
import com.leets7th.job_is_be.domain.personality.enums.PersonalityTestSource;
import jakarta.persistence.Column;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PersonalityTestRepository extends JpaRepository<PersonalityTest, Long> {

    // 완료된 가장 최신 테스트 조회
    Optional<PersonalityTest> findFirstByUserIdAndCompletedTrueOrderByStartedAtDesc(Long userId);

    // 미완료 테스트 조회 (기존)
    Optional<PersonalityTest> findFirstByUserIdAndSourceAndCompletedFalseOrderByStartedAtDesc(
            Long userId,
            PersonalityTestSource source
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select test from PersonalityTest test where test.id = :testId")
    Optional<PersonalityTest> findByIdForUpdate(@Param("testId") Long testId);
}
