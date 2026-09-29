package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.deck.entity.UserAction;
import com.leets7th.job_is_be.domain.deck.enums.ActionType;
import com.leets7th.job_is_be.domain.deck.repository.UserActionRepository;
import com.leets7th.job_is_be.domain.job.dto.ApplyClickRequest;
import com.leets7th.job_is_be.domain.job.dto.JobInteractionResponse;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * DET-01 공고 상호작용(열람/지원의향/지원하기) 기록.
 * 상태를 따로 두지 않고 UserAction 이벤트 로그의 존재 여부로 상태를 표현한다.
 * (열람/지원하기는 멱등 append, 지원의향은 토글이라 행 insert/delete)
 */
@Service
@RequiredArgsConstructor
public class JobInteractionService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final UserActionRepository userActionRepository;

    @Transactional
    public JobInteractionResponse recordView(Long userId, Long jobId) {
        Job job = getJob(jobId);

        record(userId, job, ActionType.VIEWED);

        OffsetDateTime now = OffsetDateTime.now();
        boolean applicable = isApplicable(job, now);
        boolean applyIntent = applicable && has(userId, jobId, ActionType.APPLY_INTENT_CLICKED);
        boolean applyClick = has(userId, jobId, ActionType.APPLY_CLICK);

        return response(jobId, true, applyIntent, applyClick, applicable);
    }

    @Transactional
    public JobInteractionResponse toggleApplyIntent(Long userId, Long jobId) {
        Job job = getJob(jobId);

        OffsetDateTime now = OffsetDateTime.now();
        boolean applicable = isApplicable(job, now);

        boolean applyIntent;

        if (applicable && !has(userId, jobId, ActionType.APPLY_INTENT_CLICKED)) {
            record(userId, job, ActionType.APPLY_INTENT_CLICKED);
            applyIntent = true;
        } else {
            userActionRepository.deleteByUserIdAndJobIdAndActionType(
                    userId,
                    jobId,
                    ActionType.APPLY_INTENT_CLICKED
            );
            applyIntent = false;
        }

        boolean viewed = has(userId, jobId, ActionType.VIEWED);
        boolean applyClick = has(userId, jobId, ActionType.APPLY_CLICK);

        return response(jobId, viewed, applyIntent, applyClick, applicable);
    }

    @Transactional
    public JobInteractionResponse recordApply(
            Long userId,
            Long jobId,
            ApplyClickRequest request
    ) {
        Job job = getJob(jobId);

        OffsetDateTime now = OffsetDateTime.now();
        if (!isApplicable(job, now)) {
            throw new GeneralException(ErrorStatus.JOB_NOT_APPLICABLE);
        }

        record(userId, job, ActionType.APPLY_CLICK);

        boolean applyIntent;
        if (request != null && request.applyIntent()) {
            record(userId, job, ActionType.APPLY_INTENT_CLICKED);
            applyIntent = true;
        } else {
            userActionRepository.deleteByUserIdAndJobIdAndActionType(
                    userId,
                    jobId,
                    ActionType.APPLY_INTENT_CLICKED
            );
            applyIntent = false;
        }

        boolean viewed = has(userId, jobId, ActionType.VIEWED);

        return response(jobId, viewed, applyIntent, true, true);
    }

    private boolean isApplicable(Job job, OffsetDateTime now) {
        return job.getStatus() == JobStatus.ACTIVE
                && (job.getDeadlineAt() == null || job.getDeadlineAt().isAfter(now));
    }

    /**
     * 같은 액션이 이미 존재하면 중복 저장하지 않는다.
     */
    private void record(Long userId, Job job, ActionType actionType) {
        if (!has(userId, job.getId(), actionType)) {
            userActionRepository.save(
                    UserAction.builder()
                            .user(userRepository.getReferenceById(userId))
                            .job(job)
                            .actionType(actionType)
                            .build()
            );
        }
    }

    private boolean has(Long userId, Long jobId, ActionType actionType) {
        return userActionRepository.existsByUserIdAndJobIdAndActionType(
                userId,
                jobId,
                actionType
        );
    }

    private Job getJob(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.JOB_NOT_FOUND));
    }

    private JobInteractionResponse response(
            Long jobId,
            boolean viewed,
            boolean applyIntent,
            boolean applyClick,
            boolean applicable
    ) {
        return JobInteractionResponse.of(
                jobId,
                viewed,
                applyIntent,
                applyClick,
                applicable
        );
    }
}