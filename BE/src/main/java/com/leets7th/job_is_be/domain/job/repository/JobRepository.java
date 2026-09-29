package com.leets7th.job_is_be.domain.job.repository;

import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long>, JobRepositoryCustom {

    // 지원 가능 건수 — 마감되지 않은(상시 포함) ACTIVE 공고 수
    @Query("SELECT COUNT(j) FROM Job j WHERE j.status = :status "
            + "AND (j.deadlineAt IS NULL OR j.deadlineAt > :now)")
    long countApplicable(@Param("status") JobStatus status, @Param("now") OffsetDateTime now);

    // 지원 가능한 공고 중 최신순 N건 — 추천엔진 붙기 전 임시 후보 소싱용
    @Query("SELECT j FROM Job j WHERE j.status = :status "
            + "AND (j.deadlineAt IS NULL OR j.deadlineAt > :now) "
            + "ORDER BY j.postedAt DESC")
    List<Job> findApplicableJobs(@Param("status") JobStatus status, @Param("now") OffsetDateTime now, Pageable pageable);

    // 탐색 필터 옵션 — 후보 유니버스에 실제로 존재하는 고용형태만 노출(EXP-02 §4.6 Ideal)
    @Query("SELECT DISTINCT j.employmentType FROM Job j "
            + "WHERE j.status = :status AND (j.deadlineAt IS NULL OR j.deadlineAt > :now) "
            + "AND j.employmentType IS NOT NULL ORDER BY j.employmentType")
    List<String> findDistinctEmploymentTypes(@Param("status") JobStatus status, @Param("now") OffsetDateTime now);

    // 추천엔진(파이썬)이 돌려준 external_id 목록을 서빙용 Job으로 되돌리기 위한 조회
    @Query("SELECT j FROM Job j LEFT JOIN FETCH j.company WHERE j.externalId IN :externalIds")
    List<Job> findByExternalIdIn(@Param("externalIds") List<Long> externalIds);

    Optional<Job> findBySourceAndExternalId(String source, Long externalId);

    // 출처(source)와 외부 ID(externalId) 기준 존재 여부 확인
    boolean existsBySourceAndExternalId(String source, Long externalId);

    //
    @Query("SELECT j FROM Job j " +
            "JOIN FETCH j.company " +
            "WHERE j.id <> :targetId " +
            "AND j.status = :status " +
            "AND (j.deadlineAt IS NULL OR j.deadlineAt > :now) " +
            "ORDER BY j.postedAt DESC")
    List<Job> findCandidateJobsExcludingTarget(
            @Param("targetId") Long targetId,
            @Param("status") JobStatus status,
            @Param("now") OffsetDateTime now,
            Pageable pageable
    );
}
