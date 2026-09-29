package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.dto.CareerLevelResponse;
import com.leets7th.job_is_be.domain.job.dto.JobCategoryResponse;
import com.leets7th.job_is_be.domain.job.dto.RegionResponse;
import com.leets7th.job_is_be.domain.job.dto.TechStackResponse;
import com.leets7th.job_is_be.domain.job.entity.JobCategory;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import com.leets7th.job_is_be.domain.job.enums.TechStackType;
import com.leets7th.job_is_be.domain.job.repository.JobCategoryRepository;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.domain.job.repository.RegionRepository;
import com.leets7th.job_is_be.domain.user.enums.CareerLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MetadataQueryService {

    private static final Map<String, List<String>> JOB_CATEGORY_ALIASES = Map.ofEntries(
            Map.entry("백엔드", List.of("백엔드", "백앤드", "backend", "be", "서버", "서버개발", "api")),
            Map.entry("프론트엔드", List.of("프론트엔드", "프론트", "프런트", "frontend", "front", "fe", "웹프론트", "react", "vue")),
            Map.entry("풀스택", List.of("풀스택", "풀스텍", "fullstack", "full stack")),
            Map.entry("ios", List.of("ios", "아이폰", "스위프트", "swift", "모바일", "앱")),
            Map.entry("android", List.of("android", "안드로이드", "코틀린", "kotlin", "모바일", "앱")),
            Map.entry("데이터엔지니어", List.of("데이터", "데이터엔지니어", "data engineer", "de", "빅데이터", "etl", "파이프라인")),
            Map.entry("데이터분석", List.of("데이터", "데이터분석", "분석", "data analyst", "analyst", "da")),
            Map.entry("머신러닝", List.of("머신러닝", "machine learning", "ml", "ai", "인공지능", "딥러닝", "llm", "mlops")),
            Map.entry("devops", List.of("devops", "데브옵스", "sre", "ci", "cd")),
            Map.entry("인프라", List.of("인프라", "infra", "클라우드", "cloud", "kubernetes", "쿠버네티스", "k8s", "aws")),
            Map.entry("qa", List.of("qa", "테스트", "테스터", "품질", "검증", "sdet")),
            Map.entry("보안", List.of("보안", "security", "정보보호", "시큐리티")),
            Map.entry("임베디드", List.of("임베디드", "embedded", "펌웨어", "firmware", "rtos", "mcu"))
    );

    private final RegionRepository regionRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final JobRepository jobRepository;

    public List<RegionResponse> getAllRegions() {
        return regionRepository.findAll().stream()
                .map(RegionResponse::new)
                .collect(Collectors.toList());
    }

    public List<JobCategoryResponse> getAllJobCategories() {
        return getJobCategories(null);
    }

    public List<JobCategoryResponse> getJobCategories(String query) {
        String normalizedQuery = normalize(query);
        return jobCategoryRepository.findAll().stream()
                .filter(category -> normalizedQuery.isEmpty() || matches(category.getName(), normalizedQuery))
                .sorted(Comparator
                        .comparing(JobCategory::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(JobCategory::getId, Comparator.nullsLast(Long::compareTo)))
                .map(JobCategoryResponse::new)
                .collect(Collectors.toList());
    }

    private boolean matches(String categoryName, String normalizedQuery) {
        if (normalize(categoryName).contains(normalizedQuery)) {
            return true;
        }
        String normalizedCategoryName = normalize(categoryName);
        return JOB_CATEGORY_ALIASES.entrySet().stream()
                .filter(entry -> normalizedCategoryName.contains(normalize(entry.getKey())))
                .flatMap(entry -> entry.getValue().stream())
                .map(this::normalize)
                .anyMatch(alias -> alias.startsWith(normalizedQuery));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "");
    }

    public List<CareerLevelResponse> getAllCareerLevels() {
        return Arrays.stream(CareerLevel.values())
                .map(CareerLevelResponse::new)
                .collect(Collectors.toList());
    }

    public List<String> getAllEmploymentTypes() {
        return jobRepository.findDistinctEmploymentTypes(JobStatus.ACTIVE, OffsetDateTime.now());
    }

    public List<TechStackResponse> getAllTechStacks() {
        return Arrays.stream(TechStackType.values())
                .map(TechStackResponse::from)
                .toList();
    }
}
