package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.dto.JobDetailResponse;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import com.leets7th.job_is_be.global.config.CrawlerProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 크롤링 결과 파일 파싱 -> 실제 DB 저장 -> API 응답 변환까지 전체 파이프라인을
 * 진짜 H2 DB로 검증한다(단위 테스트의 mock repository와 달리 실제 영속성 왕복 확인).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JobParserServiceIntegrationTest {

    @Autowired
    private JobParserService jobParserService;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private CrawlerProperties crawlerProperties;

    @TempDir
    Path tempDir;

    // CrawlerProperties는 Spring 컨텍스트에서 공유되는 싱글턴이라, 테스트가 바꾼 경로가
    // 컨텍스트를 재사용하는 다른 테스트로 새어나가지 않도록 원래 값을 저장/복원한다.
    private String originalCompanyOutputPath;
    private String originalJobOutputPath;

    @BeforeEach
    void saveOriginalCrawlerProperties() {
        originalCompanyOutputPath = crawlerProperties.getCompanyOutputPath();
        originalJobOutputPath = crawlerProperties.getJobOutputPath();
    }

    @AfterEach
    void restoreOriginalCrawlerProperties() {
        crawlerProperties.setCompanyOutputPath(originalCompanyOutputPath);
        crawlerProperties.setJobOutputPath(originalJobOutputPath);
    }

    @Test
    void 크롤링_결과가_저장되고_상세_API_응답까지_정상적으로_이어진다() throws IOException {
        Path companyFile = tempDir.resolve("companies.jsonl");
        Files.writeString(companyFile, "");

        Path jobFile = tempDir.resolve("jobs.jsonl");
        String jsonLine = "{\"position\":\"백엔드 엔지니어\",\"source_url\":\"https://example.com/job/999\","
                + "\"company\":{\"name\":\"통합테스트 회사\",\"normalized_name\":\"통합테스트회사\"},"
                + "\"skill_tags\":[\"Java\",\"Spring\"],\"career_min\":3,\"career_max\":10,\"is_newbie\":false,"
                + "\"employment_type\":\"regular\",\"is_remote\":true,"
                + "\"confirm_time\":\"2026-08-01\",\"due_time\":\"2026-09-01T00:00:00\","
                + "\"category_child\":[\"백엔드 개발자\"],\"intro\":\"회사 소개\","
                + "\"location_city\":\"서울\",\"location_district\":\"강남구\","
                + "\"source\":\"wanted\",\"external_id\":999}";
        Files.writeString(jobFile, jsonLine + System.lineSeparator());

        crawlerProperties.setCompanyOutputPath(companyFile.toString());
        crawlerProperties.setJobOutputPath(jobFile.toString());

        jobParserService.parseAndSave();

        Job saved = jobRepository.findBySourceAndExternalId("wanted", 999L).orElseThrow();
        JobDetailResponse response = JobDetailResponse.from(saved);

        assertThat(response.companyName()).isEqualTo("통합테스트 회사");
        assertThat(response.careerLevel()).isEqualTo("3~10년");
        assertThat(response.employmentType()).isEqualTo("regular");
        assertThat(response.remoteAvailable()).isTrue();
        assertThat(response.postedAt()).isNotNull();
        assertThat(response.dueTime()).isNotNull();
        assertThat(response.status()).isEqualTo(JobStatus.ACTIVE);
        assertThat(response.skillTags()).containsExactly("Java", "Spring");
        assertThat(saved.getCategoryChild()).containsExactly("백엔드 개발자");
        assertThat(saved.getLocationCity()).isEqualTo("서울");
        assertThat(saved.getLocationDistrict()).isEqualTo("강남구");

        // 재크롤링(같은 source+externalId) 시에도 필드가 유지/갱신되는지 확인
        jobParserService.parseAndSave();
        Job resynced = jobRepository.findBySourceAndExternalId("wanted", 999L).orElseThrow();
        assertThat(resynced.getId()).isEqualTo(saved.getId());
        assertThat(resynced.getCareerLevel()).isEqualTo("3~10년");
    }
}
