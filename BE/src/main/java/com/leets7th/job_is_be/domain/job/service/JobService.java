package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.deck.entity.UserAction;
import com.leets7th.job_is_be.domain.deck.enums.ActionType;
import com.leets7th.job_is_be.domain.deck.repository.UserActionRepository;
import com.leets7th.job_is_be.domain.job.dto.SavedJobListResponse;
import com.leets7th.job_is_be.domain.job.dto.SavedJobResponse;
import com.leets7th.job_is_be.domain.job.dto.JobDetailResponse;
import com.leets7th.job_is_be.domain.job.dto.JobSearchRequest;
import com.leets7th.job_is_be.domain.job.dto.JobSummaryResponse;
import com.leets7th.job_is_be.domain.job.dto.SimilarJobItemDto;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.entity.SavedJob;
import com.leets7th.job_is_be.domain.job.enums.JobSortType;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import com.leets7th.job_is_be.domain.job.enums.SavedJobSortType;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.domain.job.repository.SavedJobRepository;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.response.PageResponse;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hibernate.dialect.SybaseASEDialect.MAX_PAGE_SIZE;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final SavedJobRepository savedJobRepository;
    private final UserRepository userRepository;
    private final UserActionRepository userActionRepository;
    private final JobMatchingService jobMatchingService;
    private final JobSimilarService jobSimilarService;
    private final TransactionTemplate transactionTemplate;

    @Transactional
    public void saveJob(Long userId, Long jobId) {
        if (savedJobRepository.existsByUserIdAndJobId(userId, jobId)) {
            throw new GeneralException(ErrorStatus.JOB_ALREADY_SAVED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.JOB_NOT_FOUND));

        try {
            savedJobRepository.save(SavedJob.builder()
                    .user(user)
                    .job(job)
                    .savedAt(OffsetDateTime.now())
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(ErrorStatus.JOB_ALREADY_SAVED);
        }

        if (!userActionRepository.existsByUserIdAndJobIdAndActionType(userId, jobId, ActionType.SAVED)) {
            userActionRepository.save(UserAction.builder()
                    .user(user)
                    .job(job)
                    .actionType(ActionType.SAVED)
                    .build());
        }
    }

    @Transactional
    public void unsaveJob(Long userId, Long jobId) {
        if (!savedJobRepository.existsByUserIdAndJobId(userId, jobId)) {
            throw new GeneralException(ErrorStatus.JOB_NOT_SAVED);
        }

        savedJobRepository.deleteByUserIdAndJobId(userId, jobId);
        userActionRepository.deleteByUserIdAndJobIdAndActionType(userId, jobId, ActionType.SAVED);
    }

    @Transactional(readOnly = true)
    public SavedJobListResponse getSavedJobs(Long userId, int page, int size, SavedJobSortType sortType) {

        if (page < 1) {
            throw new GeneralException(ErrorStatus.INVALID_PAGE);
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new GeneralException(ErrorStatus.INVALID_PAGE_SIZE);
        }
        // page는 클라이언트에는 1부터 시작하는 값으로 노출하고, Spring Data Pageable(0-based)로는 내부에서만 변환한다.
        Pageable pageable = PageRequest.of(page - 1, size);
        OffsetDateTime now = OffsetDateTime.now();

        Page<SavedJob> savedJobs = switch (sortType) {
            case SAVED_DESC -> savedJobRepository.findByUserIdOrderBySavedDesc(userId, now, pageable);
            case DEADLINE_ASC -> savedJobRepository.findByUserIdOrderByDeadlineAsc(userId, now, pageable);
        };

        List<Long> jobIds = savedJobs.getContent().stream()
                .map(savedJob -> savedJob.getJob().getId())
                .toList();
        Set<Long> applyIntentJobIds = jobIds.isEmpty()
                ? Set.of()
                : new HashSet<>(userActionRepository.findJobIdsByUserIdAndJobIdInAndActionType(
                userId, jobIds, ActionType.APPLY_INTENT_CLICKED));

        Page<SavedJobResponse> responses = savedJobs.map(savedJob -> toSavedJobResponse(savedJob, now, applyIntentJobIds));

        long totalSaved = savedJobRepository.countByUserId(userId);
        long totalApplyIntent = savedJobRepository.countApplyIntentByUserId(userId);

        PageResponse<SavedJobResponse> pageResponse = new PageResponse<>(
                responses.getContent(),
                page,
                responses.getSize(),
                responses.getTotalElements(),
                responses.getTotalPages(),
                responses.isLast()
        );

        return new SavedJobListResponse(totalSaved, totalApplyIntent, pageResponse);
    }

    private SavedJobResponse toSavedJobResponse(SavedJob savedJob, OffsetDateTime now, Set<Long> applyIntentJobIds) {
        Job job = savedJob.getJob();
        boolean expired = job.getStatus() != JobStatus.ACTIVE
                || (job.getDeadlineAt() != null && !job.getDeadlineAt().isAfter(now));
        boolean applyIntent = applyIntentJobIds.contains(job.getId());

        return new SavedJobResponse(
                job.getId(),
                job.getCompany() != null ? job.getCompany().getName() : null,
                job.getTitle(),
                job.getLocationFull(),
                job.getCareerLevel(),
                job.getEmploymentType(),
                savedJob.getSavedAt(),
                job.getDeadlineAt(),
                expired,
                applyIntent
        );
    }

    /**
     * 채용공고 탐색 및 검색
     */
    public Page<JobSummaryResponse> searchJobs(JobSearchRequest condition, Pageable pageable, Long userId) {
        // 정렬은 Pageable 이 아니라 condition.sort(추천순/최신순/마감임박순)로 결정한다(EXP §3.4)
        Pageable fixedPageable = PageRequest.of(pageable.getPageNumber(), 24);

        // 추천순(FIT)이면 파이썬 매칭 엔진을 트랜잭션 밖에서 먼저 호출한다.
        // RecommendationService와 같은 이유 — 트랜잭션이 열린 채로 최대 300초 걸리는 Python을 기다리면
        // 커넥션 풀이 고갈될 수 있다.
        Map<Long, Integer> pythonFitScores = condition.sortOrDefault() == JobSortType.FIT
                ? fetchPythonFitScores(userId)
                : Map.of();

        return transactionTemplate.execute(status ->
                jobRepository.searchJobs(condition, fixedPageable, pythonFitScores));
    }

    /**
     * 파이썬 매칭 엔진을 호출해 추천순 정렬용 점수(externalId → 0~100)를 만든다.
     * 성향 퀴즈 미완료이거나 엔진 호출이 실패하면 빈 맵을 돌려줘서 최신순과 동일하게 대체되게 한다
     * (검색 자체가 이 호출 하나 때문에 통째로 실패하면 안 된다).
     */
    private Map<Long, Integer> fetchPythonFitScores(Long userId) {
        if (userId == null) {
            return Map.of();
        }
        try {
            List<SimilarJobItemDto> items = jobSimilarService.getRecommendedJobsByPersonality(userId).items();
            Map<Long, Integer> scores = new HashMap<>();
            for (SimilarJobItemDto item : items) {
                try {
                    scores.put(Long.valueOf(item.jobId().trim()), item.fitScore());
                } catch (NumberFormatException ignored) {
                    // 엔진이 external_id 형식이 아닌 값을 준 경우는 건너뛴다
                }
            }
            return scores;
        } catch (Exception e) {
            log.warn("[JobService] 추천순 정렬용 파이썬 매칭 엔진 호출 실패. 최신순으로 대체합니다. userId={}", userId, e);
            return Map.of();
        }
    }

    /**
     * 채용공고 단건 상세 조회
     * - 존재하지 않을 경우 JOB_NOT_FOUND 예외 발생
     */
    @Transactional(readOnly = true)
    /**
     * 공고 상세 조회. 인증이 필요한 경로이므로 userId 는 항상 존재한다.
     * 성향 퀴즈 미완료 사용자는 매칭 정보만 null 로 내려간다.
     */
    public JobDetailResponse getJobDetail(Long jobId, Long userId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.JOB_NOT_FOUND));

        return JobDetailResponse.from(job, jobMatchingService.resolve(userId, job));
    }
}
