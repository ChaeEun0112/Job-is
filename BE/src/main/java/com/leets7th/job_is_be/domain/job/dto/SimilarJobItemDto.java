package com.leets7th.job_is_be.domain.job.dto;

import java.util.List;

public record SimilarJobItemDto(
        String jobId,
        String title,
        String companyName,
        int fitScore, // 0~100 범위 정수
        String reason, // 1문장
        List<String> fitPoints, // 근거 배열
        CriteriaMatrixDto criteriaMatrix
) {}
