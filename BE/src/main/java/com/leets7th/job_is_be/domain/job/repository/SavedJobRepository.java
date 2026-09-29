package com.leets7th.job_is_be.domain.job.repository;

import com.leets7th.job_is_be.domain.job.entity.SavedJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    boolean existsByUserIdAndJobId(Long userId, Long jobId);

    Optional<SavedJob> findByUserIdAndJobId(Long userId, Long jobId);

    void deleteByUserIdAndJobId(Long userId, Long jobId);

    long countByUserId(Long userId);

    @Query(value = """
            SELECT sj FROM SavedJob sj
            JOIN FETCH sj.job j
            LEFT JOIN FETCH j.company
            WHERE sj.user.id = :userId
            ORDER BY
                CASE WHEN j.status <> com.leets7th.job_is_be.domain.job.enums.JobStatus.ACTIVE
                     OR (j.deadlineAt IS NOT NULL AND j.deadlineAt <= :now)
                     THEN 1 ELSE 0 END ASC,
                sj.savedAt DESC
            """,
            countQuery = "SELECT COUNT(sj) FROM SavedJob sj WHERE sj.user.id = :userId")
    Page<SavedJob> findByUserIdOrderBySavedDesc(@Param("userId") Long userId, @Param("now") OffsetDateTime now, Pageable pageable);

    @Query(value = """
            SELECT sj FROM SavedJob sj
            JOIN FETCH sj.job j
            LEFT JOIN FETCH j.company
            WHERE sj.user.id = :userId
            ORDER BY
                CASE WHEN j.status <> com.leets7th.job_is_be.domain.job.enums.JobStatus.ACTIVE
                     OR (j.deadlineAt IS NOT NULL AND j.deadlineAt <= :now)
                     THEN 1 ELSE 0 END ASC,
                j.deadlineAt ASC NULLS LAST,
                sj.savedAt DESC
            """,
            countQuery = "SELECT COUNT(sj) FROM SavedJob sj WHERE sj.user.id = :userId")
    Page<SavedJob> findByUserIdOrderByDeadlineAsc(@Param("userId") Long userId, @Param("now") OffsetDateTime now, Pageable pageable);

    @Query("""
            SELECT COUNT(sj) FROM SavedJob sj
            WHERE sj.user.id = :userId
            AND EXISTS (
                SELECT 1 FROM UserAction ua
                WHERE ua.user.id = sj.user.id
                AND ua.job.id = sj.job.id
                AND ua.actionType = com.leets7th.job_is_be.domain.deck.enums.ActionType.APPLY_INTENT_CLICKED
            )
            """)
    long countApplyIntentByUserId(@Param("userId") Long userId);
}
