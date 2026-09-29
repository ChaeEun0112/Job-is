package com.leets7th.job_is_be.domain.user.dto;

import com.leets7th.job_is_be.domain.user.enums.CareerLevel;
import com.leets7th.job_is_be.domain.user.enums.OnboardingStep;

import java.util.List;

public record ProfileDraftResponse(
        OnboardingStep onboardingStep,
        List<ProfileJobCategoryResponse> jobCategories,
        ProfileRegionResponse region,
        boolean remoteOk,
        CareerLevel careerLevel,
        List<String> preferenceNotes,
        List<String> excludeKeywords,
        List<String> techStacks,
        List<String> personalityTags,
        boolean jobTestCompleted
) {
}
