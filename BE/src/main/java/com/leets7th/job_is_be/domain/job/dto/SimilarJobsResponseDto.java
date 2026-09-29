package com.leets7th.job_is_be.domain.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 성향 퀴즈 결과를 기준으로 산출한 추천 공고 묶음.
 *
 * <p>기준이 되는 것은 <b>사용자</b>이지 특정 공고가 아니다. 사용자 식별은 요청의 JWT로 끝나므로
 * 응답에 사용자·공고 식별자를 따로 싣지 않는다.
 *
 * <p>과거 이 자리에 {@code targetJobId} 필드가 있었으나 실제로 담기던 값은 userId 였고,
 * 그 이름 탓에 "공고 기준 유사 추천"으로 오인될 소지가 있어 필드를 제거했다.
 */
@Schema(description = "성향 퀴즈 기반 추천 공고 묶음 (기준: 요청 JWT의 로그인 사용자)")
public record SimilarJobsResponseDto(
        @Schema(description = "묶음 라벨", example = "추천")
        String label,

        @Schema(description = "추천 공고 목록")
        List<SimilarJobItemDto> items
) {
    public static SimilarJobsResponseDto of(List<SimilarJobItemDto> items) {
        return new SimilarJobsResponseDto("추천", items);
    }
}
