package com.leets7th.job_is_be.domain.user.dto;

import com.leets7th.job_is_be.domain.user.enums.CareerLevel;

import java.util.List;

/**
 * 프로필 수정 요청. null 필드는 "변경 없음"을 뜻한다.
 * 희망 지역은 단일 선택이며 원격 포함 여부는 별도 토글이다.
 */
public record ProfileUpdateRequest(
        List<Long> jobCategoryIds,
        Long primaryJobCategoryId,
        Long regionId,
        Boolean remoteOk,
        CareerLevel careerLevel,
        List<String> preferenceNotes,
        List<String> excludeKeywords,
        List<String> techStacks
) {
}
