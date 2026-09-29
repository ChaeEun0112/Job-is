package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.deck.enums.ActionType;
import com.leets7th.job_is_be.domain.deck.repository.UserActionRepository;
import com.leets7th.job_is_be.domain.job.dto.ApplyClickRequest;
import com.leets7th.job_is_be.domain.job.dto.JobInteractionResponse;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobInteractionServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserActionRepository userActionRepository;

    @InjectMocks
    private JobInteractionService jobInteractionService;

    private Job job(OffsetDateTime deadlineAt) {
        Job job = Job.builder()
                .title("백엔드 엔지니어")
                .deadlineAt(deadlineAt)
                .build();
        ReflectionTestUtils.setField(job, "id", 10L);
        return job;
    }

    private void stubHas(ActionType actionType, boolean value) {
        lenient().when(userActionRepository.existsByUserIdAndJobIdAndActionType(1L, 10L, actionType))
                .thenReturn(value);
    }

    private void stubReferenceUser() {
        User user = User.builder()
                .socialId("s")
                .socialType(SocialType.KAKAO)
                .email("a@a.com")
                .build();

        lenient().when(userRepository.getReferenceById(1L))
                .thenReturn(user);
    }

    @Test
    void givenActiveJob_whenRecordView_thenSaveViewedAction() {
        when(jobRepository.findById(10L))
                .thenReturn(Optional.of(job(OffsetDateTime.now().plusDays(3))));
        stubReferenceUser();
        stubHas(ActionType.VIEWED, false);
        stubHas(ActionType.APPLY_INTENT_CLICKED, false);
        stubHas(ActionType.APPLY_CLICK, false);

        JobInteractionResponse response = jobInteractionService.recordView(1L, 10L);

        assertThat(response.viewed()).isTrue();
        assertThat(response.applyIntent()).isFalse();
        assertThat(response.applied()).isFalse();

        verify(userActionRepository).save(any());
    }

    @Test
    void givenViewedJob_whenRecordViewAgain_thenDoNotSaveViewedAction() {
        when(jobRepository.findById(10L))
                .thenReturn(Optional.of(job(OffsetDateTime.now().plusDays(3))));
        stubHas(ActionType.VIEWED, true);
        stubHas(ActionType.APPLY_INTENT_CLICKED, false);
        stubHas(ActionType.APPLY_CLICK, false);

        jobInteractionService.recordView(1L, 10L);

        verify(userActionRepository, never()).save(any());
    }

    @Test
    void givenActiveJobWithoutApplyIntent_whenToggleApplyIntent_thenSaveApplyIntent() {
        when(jobRepository.findById(10L))
                .thenReturn(Optional.of(job(OffsetDateTime.now().plusDays(3))));
        stubReferenceUser();
        stubHas(ActionType.APPLY_INTENT_CLICKED, false);
        stubHas(ActionType.VIEWED, false);
        stubHas(ActionType.APPLY_CLICK, false);

        JobInteractionResponse response = jobInteractionService.toggleApplyIntent(1L, 10L);

        assertThat(response.applyIntent()).isTrue();
        assertThat(response.applicable()).isTrue();

        verify(userActionRepository).save(any());
    }

    @Test
    void givenJobWithApplyIntent_whenToggleApplyIntent_thenDeleteApplyIntent() {
        when(jobRepository.findById(10L))
                .thenReturn(Optional.of(job(OffsetDateTime.now().plusDays(3))));
        stubHas(ActionType.APPLY_INTENT_CLICKED, true);
        stubHas(ActionType.VIEWED, false);
        stubHas(ActionType.APPLY_CLICK, false);

        JobInteractionResponse response = jobInteractionService.toggleApplyIntent(1L, 10L);

        assertThat(response.applyIntent()).isFalse();

        verify(userActionRepository)
                .deleteByUserIdAndJobIdAndActionType(
                        1L,
                        10L,
                        ActionType.APPLY_INTENT_CLICKED
                );
    }

    @Test
    void givenExpiredJob_whenToggleApplyIntent_thenAlwaysDisableApplyIntent() {
        when(jobRepository.findById(10L))
                .thenReturn(Optional.of(job(OffsetDateTime.now().minusDays(1))));
        stubHas(ActionType.VIEWED, false);
        stubHas(ActionType.APPLY_CLICK, false);

        JobInteractionResponse response = jobInteractionService.toggleApplyIntent(1L, 10L);

        assertThat(response.applyIntent()).isFalse();
        assertThat(response.applicable()).isFalse();

        verify(userActionRepository)
                .deleteByUserIdAndJobIdAndActionType(
                        1L,
                        10L,
                        ActionType.APPLY_INTENT_CLICKED
                );
        verify(userActionRepository, never()).save(any());
    }

    @Test
    void givenApplyIntentChecked_whenRecordApply_thenSaveApplyClickAndApplyIntent() {
        when(jobRepository.findById(10L))
                .thenReturn(Optional.of(job(OffsetDateTime.now().plusDays(3))));
        stubReferenceUser();
        stubHas(ActionType.APPLY_CLICK, false);
        stubHas(ActionType.APPLY_INTENT_CLICKED, false);
        stubHas(ActionType.VIEWED, false);

        JobInteractionResponse response =
                jobInteractionService.recordApply(1L, 10L, new ApplyClickRequest(true));

        assertThat(response.applied()).isTrue();
        assertThat(response.applyIntent()).isTrue();

        verify(userActionRepository, times(2)).save(any());
    }

    @Test
    void givenApplyIntentUnchecked_whenRecordApply_thenSaveOnlyApplyClick() {
        when(jobRepository.findById(10L))
                .thenReturn(Optional.of(job(OffsetDateTime.now().plusDays(3))));
        stubReferenceUser();
        stubHas(ActionType.APPLY_CLICK, false);
        stubHas(ActionType.APPLY_INTENT_CLICKED, false);
        stubHas(ActionType.VIEWED, false);

        JobInteractionResponse response =
                jobInteractionService.recordApply(1L, 10L, new ApplyClickRequest(false));

        assertThat(response.applied()).isTrue();
        assertThat(response.applyIntent()).isFalse();

        verify(userActionRepository, times(1)).save(any());
    }

    @Test
    void givenExpiredJob_whenRecordApply_thenThrowGeneralException() {
        when(jobRepository.findById(10L))
                .thenReturn(Optional.of(job(OffsetDateTime.now().minusDays(1))));

        assertThatThrownBy(() ->
                jobInteractionService.recordApply(1L, 10L, new ApplyClickRequest(false)))
                .isInstanceOf(GeneralException.class);

        verify(userActionRepository, never()).save(any());
    }

    @Test
    void givenNonExistingJob_whenRecordView_thenThrowGeneralException() {
        when(jobRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                jobInteractionService.recordView(1L, 10L))
                .isInstanceOf(GeneralException.class);
    }
}