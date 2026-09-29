package com.leets7th.job_is_be.domain.job.dto;

/**
 * 지원하기 클릭 요청 (DET-01, 원티드 이동 확인 모달)
 * applyIntent: "이 공고에 지원 의향도 표시할게요" 체크박스 (선택·기본 꺼짐)
 */
public record ApplyClickRequest(
        boolean applyIntent
) {
}
