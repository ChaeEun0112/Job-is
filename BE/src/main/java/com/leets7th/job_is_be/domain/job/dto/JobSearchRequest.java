package com.leets7th.job_is_be.domain.job.dto;

import com.leets7th.job_is_be.domain.job.enums.CareerRange;
import com.leets7th.job_is_be.domain.job.enums.JobSortType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

/**
 * 탐색 검색 조건 (화면설계서 EXP-02 §4.2 필터 6종 + §4.3 정렬).
 * 스킬(skill_tags)은 §2.4에 따라 독립 필터로 제공하지 않고 키워드 검색 매칭에만 사용한다.
 * 급여 축은 §2.3에 따라 제공하지 않는다.
 */
@Builder
public record JobSearchRequest(
        @Schema(description = "키워드 — 공고명·회사명·스킬 태그 부분 매칭")
        String keyword,

        @Schema(description = "세부직군 목록 (다중, OR)")
        List<String> categoryChildren,

        @Schema(description = "지역 시·도 목록 (다중, OR). 선택 가능한 값은 /api/jobs/filters/regions 로 조회한다.",
                example = "[\"서울\", \"경기\"]")
        List<String> cities,

        @Schema(description = "지역 구/군 목록 (다중, OR). 시·도를 선택했을 때만 의미가 있다.")
        List<String> districts,

        @Schema(description = "경력 구간 목록 (다중, OR)")
        List<CareerRange> careerRanges,

        @Schema(description = "고용형태 목록 (다중, OR)", example = "[\"정규직\"]")
        List<String> employmentTypes,

        @Schema(description = "원격 가능 공고만 보기", defaultValue = "false")
        Boolean remoteOnly,

        @Schema(description = "상시채용(마감일 없음) 포함 여부", defaultValue = "true")
        Boolean includeAlwaysOpen,

        @Schema(description = "정렬 — FIT(추천순, 기본) / RECENT(최신순) / DEADLINE(마감임박순)",
                defaultValue = "FIT")
        JobSortType sort
) {
    public boolean remoteOnlyOrFalse() {
        return Boolean.TRUE.equals(remoteOnly);
    }

    /** 미지정 시 상시채용 포함이 기본값(§4.2). */
    public boolean includeAlwaysOpenOrDefault() {
        return includeAlwaysOpen == null || includeAlwaysOpen;
    }

    /** 미지정 시 추천순이 기본값(§3.4). */
    public JobSortType sortOrDefault() {
        return sort == null ? JobSortType.FIT : sort;
    }
}
