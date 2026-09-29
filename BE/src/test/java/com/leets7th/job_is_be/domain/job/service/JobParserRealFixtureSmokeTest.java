package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.dto.JobDetailResponse;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.global.config.CrawlerProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 임시 검증용: 실제 크롤러가 생성한 database/out/job_postings.jsonl 전체를 파싱해서
 * 예외 없이 끝까지 돌고, 핵심 필드가 실제로 채워지는지 확인한다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JobParserRealFixtureSmokeTest {

    private static final String REAL_JOB_FILE = "database/out/job_postings.jsonl";
    private static final String REAL_COMPANY_FILE = "database/out/companies.jsonl";

    @Autowired
    private JobParserService jobParserService;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private CrawlerProperties crawlerProperties;

    // CrawlerProperties는 Spring 컨텍스트에서 공유되는 싱글턴이라, 테스트가 바꾼 경로가
    // 컨텍스트를 재사용하는 다른 테스트로 새어나가지 않도록 원래 값을 저장/복원한다.
    private String originalCompanyOutputPath;
    private String originalJobOutputPath;

    static boolean realFixtureExists() {
        return new File(REAL_JOB_FILE).exists() && new File(REAL_COMPANY_FILE).exists();
    }

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
    @EnabledIf("realFixtureExists")
    void 실제_크롤러_출력_파일이_예외_없이_전부_파싱되고_핵심_필드가_채워진다() {
        crawlerProperties.setCompanyOutputPath(Path.of(REAL_COMPANY_FILE).toAbsolutePath().toString());
        crawlerProperties.setJobOutputPath(Path.of(REAL_JOB_FILE).toAbsolutePath().toString());

        jobParserService.parseAndSave();

        long savedCount = jobRepository.count();
        assertThat(savedCount).isGreaterThan(0);

        Job sample = jobRepository.findAll().get(0);
        JobDetailResponse response = JobDetailResponse.from(sample);

        assertThat(sample.getEmploymentType()).isNotNull();
        assertThat(sample.getCareerLevel()).isNotNull();
        assertThat(sample.getExternalId()).isNotNull();
        assertThat(response).isNotNull(); // 기업 보강 여부와 무관하게 예외 없이 응답 변환은 되어야 함
    }
}
