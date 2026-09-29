package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.deck.entity.UserAction;
import com.leets7th.job_is_be.domain.deck.enums.ActionType;
import com.leets7th.job_is_be.domain.deck.repository.UserActionRepository;
import com.leets7th.job_is_be.domain.job.entity.Company;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.enums.HistoryActionType;
import com.leets7th.job_is_be.domain.job.enums.HistoryFilterType;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.response.PageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @Mock
    private UserActionRepository userActionRepository;

    @InjectMocks
    private HistoryService historyService;

    private Job job(Long id, OffsetDateTime deadlineAt, JobStatus status) {
        Job job = Job.builder()
                .company(Company.builder().name("잡이즈").build())
                .title("백엔드 엔지니어")
                .deadlineAt(deadlineAt)
                .build();
        ReflectionTestUtils.setField(job, "id", id);
        ReflectionTestUtils.setField(job, "status", status);
        return job;
    }

    private UserAction userAction(Job job, ActionType actionType, String reasonCode, String comment) {
        UserAction action = UserAction.builder()
                .job(job)
                .actionType(actionType)
                .reasonCode(reasonCode)
                .comment(comment)
                .build();
        ReflectionTestUtils.setField(action, "createdAt", java.time.OffsetDateTime.now());
        return action;
    }

    @Test
    void givenPageLessThanOne_whenGetHistory_thenThrowGeneralException() {
        assertThatThrownBy(() -> historyService.getHistory(1L, 0, 24, HistoryFilterType.ALL))
                .isInstanceOf(GeneralException.class);
    }

    @Test
    void givenSizeOutOfRange_whenGetHistory_thenThrowGeneralException() {
        assertThatThrownBy(() -> historyService.getHistory(1L, 1, 0, HistoryFilterType.ALL))
                .isInstanceOf(GeneralException.class);
    }

    @Test
    void givenSkippedActionWithReason_whenGetHistory_thenMapToSkippedItemWithReason() {
        Job job = job(10L, OffsetDateTime.now().plusDays(3), JobStatus.ACTIVE);
        UserAction dismissed = userAction(job, ActionType.DISMISSED, "REGION_MISMATCH", "지역이 안 맞아요");

        Page<UserAction> page = new PageImpl<>(List.of(dismissed));
        when(userActionRepository.findLatestByUserIdAndActionTypeIn(any(), any(), any()))
                .thenReturn(page);

        PageResponse<?> response = historyService.getHistory(1L, 1, 24, HistoryFilterType.SKIPPED);

        assertThat(response.content()).hasSize(1);
        var item = (com.leets7th.job_is_be.domain.job.dto.HistoryItemResponse) response.content().get(0);
        assertThat(item.jobId()).isEqualTo(10L);
        assertThat(item.companyName()).isEqualTo("잡이즈");
        assertThat(item.actionType()).isEqualTo(HistoryActionType.SKIPPED);
        assertThat(item.reasonCode()).isEqualTo("REGION_MISMATCH");
        assertThat(item.comment()).isEqualTo("지역이 안 맞아요");
        assertThat(item.expired()).isFalse();
    }

    @Test
    void givenExpiredJob_whenGetHistory_thenMarkItemExpired() {
        Job job = job(11L, OffsetDateTime.now().minusDays(1), JobStatus.ACTIVE);
        UserAction viewed = userAction(job, ActionType.VIEWED, null, null);

        Page<UserAction> page = new PageImpl<>(List.of(viewed));
        when(userActionRepository.findLatestByUserIdAndActionTypeIn(any(), any(), any()))
                .thenReturn(page);

        PageResponse<?> response = historyService.getHistory(1L, 1, 24, HistoryFilterType.VIEWED);

        var item = (com.leets7th.job_is_be.domain.job.dto.HistoryItemResponse) response.content().get(0);
        assertThat(item.expired()).isTrue();
    }

    @Test
    void givenAllFilter_whenGetHistory_thenQueryAllFourActionTypes() {
        when(userActionRepository.findLatestByUserIdAndActionTypeIn(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        historyService.getHistory(1L, 1, 24, HistoryFilterType.ALL);

        ArgumentCaptor<List<ActionType>> captor = ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(userActionRepository)
                .findLatestByUserIdAndActionTypeIn(anyLong(), captor.capture(), any(Pageable.class));

        assertThat(captor.getValue())
                .containsExactlyInAnyOrder(ActionType.VIEWED, ActionType.DISMISSED, ActionType.APPLY_INTENT_CLICKED, ActionType.SAVED);
    }

    @Test
    void givenSavedAction_whenGetHistory_thenMapToSavedItem() {
        Job job = job(12L, OffsetDateTime.now().plusDays(3), JobStatus.ACTIVE);
        UserAction saved = userAction(job, ActionType.SAVED, null, null);

        Page<UserAction> page = new PageImpl<>(List.of(saved));
        when(userActionRepository.findLatestByUserIdAndActionTypeIn(any(), any(), any()))
                .thenReturn(page);

        PageResponse<?> response = historyService.getHistory(1L, 1, 24, HistoryFilterType.SAVED);

        var item = (com.leets7th.job_is_be.domain.job.dto.HistoryItemResponse) response.content().get(0);
        assertThat(item.actionType()).isEqualTo(HistoryActionType.SAVED);
    }
}
