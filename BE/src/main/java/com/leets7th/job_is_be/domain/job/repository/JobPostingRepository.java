package com.leets7th.job_is_be.domain.job.repository;

import com.leets7th.job_is_be.domain.job.entity.JobPosting;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.Repository;

import java.util.Optional;

// 크롤러가 적재하는 job_postings 원문 테이블. 앱은 읽기만 하므로 JpaRepository 대신 Repository로 제한
public interface JobPostingRepository extends Repository<JobPosting, Long> {

    Slice<JobPosting> findAll(Pageable pageable);

    // (source, externalId)로 Job과 매칭되는 원문 조회. 재수집으로 중복될 수 있어 최신 수집본 1건만
    Optional<JobPosting> findFirstBySourceAndExternalIdOrderByCollectedAtDesc(String source, Long externalId);
}
