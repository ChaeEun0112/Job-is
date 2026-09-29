package com.leets7th.job_is_be.domain.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 공고 상세의 사용자 매칭 정보 (화면설계서 DET-01 ④ 당신의 기준 / ⑥ 왜 추천했나).
 *
 * <p>비로그인이거나 성향 퀴즈 미완료인 사용자에게는 이 객체 자체가 null 로 내려간다.
 */
@Schema(description = "로그인 사용자 기준 공고 매칭 정보. 비로그인·퀴즈 미완료 시 null")
public record JobMatchingResponse(
        @Schema(description = "적합도 점수 (0~100)", example = "82")
        Integer matchScore,

        @Schema(description = "적합도 별점 (0.0~5.0, 소수 첫째자리). matchScore 를 5점 만점으로 환산", example = "4.1")
        Double rating,

        @Schema(description = "매칭 근거 문장. 근거가 없으면 빈 배열")
        List<String> matchReasons,

        @Schema(description = "적합도 체크리스트 6축 판정")
        CriteriaMatrixDto fitCriteria
) {
    private static final int MAX_SCORE = 100;
    private static final double RATING_FULL_MARK = 5.0;

    public static JobMatchingResponse of(int matchScore, List<String> matchReasons, CriteriaMatrixDto fitCriteria) {
        int bounded = Math.max(0, Math.min(MAX_SCORE, matchScore));
        return new JobMatchingResponse(
                bounded,
                toRating(bounded),
                matchReasons == null ? List.of() : matchReasons,
                fitCriteria == null ? CriteriaMatrixDto.createDefault() : fitCriteria
        );
    }

    /**
     * 별점 기준을 백엔드 한 곳에 고정한다(화면마다 반올림이 갈리지 않도록).
     * 10배 후 정수 반올림해 되나누므로 부동소수점 잔여 소수(4.00000001 등)가 남지 않는다.
     */
    private static double toRating(int matchScore) {
        double raw = matchScore * RATING_FULL_MARK / MAX_SCORE;
        return Math.round(raw * 10) / 10.0;
    }
}
