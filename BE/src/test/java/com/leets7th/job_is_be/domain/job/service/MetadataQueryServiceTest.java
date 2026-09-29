package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.dto.JobCategoryResponse;
import com.leets7th.job_is_be.domain.job.entity.JobCategory;
import com.leets7th.job_is_be.domain.job.repository.JobCategoryRepository;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.domain.job.repository.RegionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetadataQueryServiceTest {

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private JobCategoryRepository jobCategoryRepository;

    @Mock
    private JobRepository jobRepository;

    private MetadataQueryService metadataQueryService;

    @BeforeEach
    void setUp() {
        metadataQueryService = new MetadataQueryService(
                regionRepository,
                jobCategoryRepository,
                jobRepository
        );
        when(jobCategoryRepository.findAll()).thenReturn(categories());
    }

    @Test
    void returnsAllCategoriesInSortOrderWhenQueryIsMissing() {
        List<JobCategoryResponse> response = metadataQueryService.getJobCategories(null);

        assertEquals(5, response.size());
        assertEquals("백엔드 개발자", response.get(0).name());
        assertEquals("머신러닝 엔지니어(ML)", response.get(4).name());
    }

    @Test
    void findsBackendCategoryByTypoAndEnglishAlias() {
        List<JobCategoryResponse> typoResponse = metadataQueryService.getJobCategories(" 백 앤드 ");
        List<JobCategoryResponse> englishResponse = metadataQueryService.getJobCategories("BACKEND");

        assertEquals(List.of("백엔드 개발자"), names(typoResponse));
        assertEquals(List.of("백엔드 개발자"), names(englishResponse));
    }

    @Test
    void returnsEveryMatchingDataCategory() {
        List<JobCategoryResponse> response = metadataQueryService.getJobCategories("데이터");

        assertEquals(List.of(
                "데이터 엔지니어(DE)",
                "데이터 분석가(DA)"
        ), names(response));
    }

    @Test
    void keepsAliasSearchWhenDatabaseCategorySuffixChanges() {
        when(jobCategoryRepository.findAll()).thenReturn(List.of(
                category("백엔드 엔지니어", "백엔드", 1),
                category("데이터 엔지니어", "데이터", 2)
        ));

        assertEquals(
                List.of("백엔드 엔지니어"),
                names(metadataQueryService.getJobCategories("backend"))
        );
        assertEquals(
                List.of("데이터 엔지니어"),
                names(metadataQueryService.getJobCategories("data engineer"))
        );
    }

    @Test
    void returnsEmptyListWhenNoCategoryMatches() {
        List<JobCategoryResponse> response = metadataQueryService.getJobCategories("회계사");

        assertTrue(response.isEmpty());
    }

    private List<String> names(List<JobCategoryResponse> responses) {
        return responses.stream().map(JobCategoryResponse::name).toList();
    }

    private List<JobCategory> categories() {
        return List.of(
                category("데이터 분석가(DA)", "데이터", 7),
                category("머신러닝 엔지니어(ML)", "데이터", 8),
                category("백엔드 개발자", "백엔드", 1),
                category("프론트엔드 개발자", "프론트엔드", 2),
                category("데이터 엔지니어(DE)", "데이터", 6)
        );
    }

    private JobCategory category(String name, String groupName, int sortOrder) {
        return JobCategory.builder()
                .name(name)
                .groupName(groupName)
                .sortOrder(sortOrder)
                .build();
    }
}
