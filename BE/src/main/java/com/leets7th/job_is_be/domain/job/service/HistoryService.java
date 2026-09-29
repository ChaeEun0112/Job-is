package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.deck.entity.UserAction;
import com.leets7th.job_is_be.domain.deck.repository.UserActionRepository;
import com.leets7th.job_is_be.domain.job.dto.HistoryItemResponse;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.enums.HistoryActionType;
import com.leets7th.job_is_be.domain.job.enums.HistoryFilterType;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.response.PageResponse;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserActionRepository userActionRepository;

    @Transactional(readOnly = true)
    public PageResponse<HistoryItemResponse> getHistory(Long userId, int page, int size, HistoryFilterType filter) {
        if (page < 1) {
            throw new GeneralException(ErrorStatus.INVALID_PAGE);
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new GeneralException(ErrorStatus.INVALID_PAGE_SIZE);
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        OffsetDateTime now = OffsetDateTime.now();

        Page<UserAction> actions = userActionRepository.findLatestByUserIdAndActionTypeIn(
                userId, filter.toActionTypes(), pageable);

        Page<HistoryItemResponse> responses = actions.map(action -> toHistoryItemResponse(action, now));

        return new PageResponse<>(
                responses.getContent(),
                page,
                responses.getSize(),
                responses.getTotalElements(),
                responses.getTotalPages(),
                responses.isLast()
        );
    }

    private HistoryItemResponse toHistoryItemResponse(UserAction action, OffsetDateTime now) {
        Job job = action.getJob();
        boolean expired = job.getStatus() != JobStatus.ACTIVE
                || (job.getDeadlineAt() != null && !job.getDeadlineAt().isAfter(now));

        return new HistoryItemResponse(
                job.getId(),
                job.getCompany() != null ? job.getCompany().getName() : null,
                job.getTitle(),
                HistoryActionType.from(action.getActionType()),
                action.getReasonCode(),
                action.getComment(),
                action.getCreatedAt(),
                expired
        );
    }
}
