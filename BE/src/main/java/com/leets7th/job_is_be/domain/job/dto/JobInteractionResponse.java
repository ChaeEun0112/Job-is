package com.leets7th.job_is_be.domain.job.dto;

/**
 * 공고 상호작용 상태 응답 (DET-01) — UserAction 이벤트 존재 여부로 도출
 * applicable = 마감/무효가 아니라 지원·지원의향 표시가 가능한 상태인지 여부 (마감 시 프런트 버튼 비활성 근거)
 */
public record JobInteractionResponse(
        Long jobId,
        boolean viewed,
        boolean applyIntent,
        boolean applied,
        boolean applicable
) {
    public static JobInteractionResponse of(Long jobId, boolean viewed, boolean applyIntent,
                                            boolean applied, boolean applicable) {
        return new JobInteractionResponse(jobId, viewed, applyIntent, applied, applicable);
    }
}
