package com.leets7th.job_is_be.domain.job.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.leets7th.job_is_be.domain.job.entity.Company;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.repository.JobCategoryRepository;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.domain.job.repository.RegionRepository;
import com.leets7th.job_is_be.global.config.CrawlerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 픽스처는 실제 크롤러(wanted.py normalize())가 내려주는 JSON 키/형식을 그대로 따른다
 * (스네이크케이스, confirm_time="yyyy-MM-dd", due_time="yyyy-MM-dd'T'HH:mm:ss").
 */
@ExtendWith(MockitoExtension.class)
class JobParserServiceTest {

    @Mock
    private CompanyService companyService;
    @Mock
    private JobCategoryRepository jobCategoryRepository;
    @Mock
    private RegionRepository regionRepository;
    @Mock
    private JobRepository jobRepository;

    @TempDir
    Path tempDir;

    private JobParserService jobParserService;
    private CrawlerProperties crawlerProperties;

    @BeforeEach
    void setUp() {
        crawlerProperties = new CrawlerProperties();
        jobParserService = new JobParserService(
                crawlerProperties, companyService, jobCategoryRepository,
                regionRepository, jobRepository, new ObjectMapper().registerModule(new JavaTimeModule())
        );
    }

    @Test
    void 신규_공고_저장_시_실제_크롤러_필드명_기준으로_전부_채워진다() throws IOException {
        Path companyFile = tempDir.resolve("companies.jsonl");
        Files.writeString(companyFile, "");

        Path jobFile = tempDir.resolve("jobs.jsonl");
        String jsonLine = "{\"position\":\"백엔드 엔지니어\",\"source_url\":\"https://example.com/job/1\","
                + "\"reward_total\":\"100만원\",\"skill_tags\":[\"Java\",\"Spring\"],"
                + "\"category_child\":[\"백엔드 개발자\"],\"category_parent\":\"개발\","
                + "\"main_tasks\":\"주요업무\",\"preferred_points\":\"우대사항\","
                + "\"career_min\":3,\"career_max\":10,\"is_newbie\":false,"
                + "\"source\":\"wanted\",\"external_id\":123,"
                + "\"employment_type\":\"regular\",\"is_remote\":true,"
                + "\"confirm_time\":\"2026-08-01\",\"due_time\":\"2026-09-01T00:00:00\","
                + "\"location_city\":\"서울\",\"location_district\":\"강남구\","
                + "\"location_full\":\"서울 강남구 테헤란로\","
                + "\"thumbnail_url\":\"https://example.com/thumb.png\"}";
        Files.writeString(jobFile, jsonLine + System.lineSeparator());

        crawlerProperties.setCompanyOutputPath(companyFile.toString());
        crawlerProperties.setJobOutputPath(jobFile.toString());

        when(companyService.getOrCreateCompany(any())).thenReturn(testCompany());
        when(jobRepository.findBySourceAndExternalId("wanted", 123L)).thenReturn(Optional.empty());

        jobParserService.parseAndSave();

        ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(captor.capture());
        Job saved = captor.getValue();

        assertThat(saved.getCareerLevel()).isEqualTo("3~10년");
        assertThat(saved.getEmploymentType()).isEqualTo("regular");
        assertThat(saved.getRemoteAvailable()).isTrue();
        assertThat(saved.getPostedAt()).isEqualTo(OffsetDateTime.of(2026, 8, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)));
        assertThat(saved.getDeadlineAt()).isEqualTo(OffsetDateTime.of(2026, 9, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9)));
        assertThat(saved.getSkillTags()).containsExactly("Java", "Spring");
        assertThat(saved.getCategoryChild()).containsExactly("백엔드 개발자");
        assertThat(saved.getMainTasks()).isEqualTo("주요업무");
        assertThat(saved.getPreferredPoints()).isEqualTo("우대사항");
        assertThat(saved.getCareerMin()).isEqualTo(3);
        assertThat(saved.getCareerMax()).isEqualTo(10);
        assertThat(saved.getLocationCity()).isEqualTo("서울");
        assertThat(saved.getLocationDistrict()).isEqualTo("강남구");
        assertThat(saved.getLocationFull()).isEqualTo("서울 강남구 테헤란로");
        assertThat(saved.getThumbnailUrl()).isEqualTo("https://example.com/thumb.png");
        assertThat(saved.getExternalId()).isEqualTo(123L);
    }

    @Test
    void 신입_여부만_있고_경력연수가_없으면_신입으로_계산된다() throws IOException {
        Path companyFile = tempDir.resolve("companies.jsonl");
        Files.writeString(companyFile, "");

        Path jobFile = tempDir.resolve("jobs.jsonl");
        String jsonLine = "{\"position\":\"주니어 백엔드\",\"source_url\":\"https://example.com/job/2\","
                + "\"is_newbie\":true,\"source\":\"wanted\",\"external_id\":456,"
                + "\"employment_type\":\"regular\",\"is_remote\":false}";
        Files.writeString(jobFile, jsonLine + System.lineSeparator());

        crawlerProperties.setCompanyOutputPath(companyFile.toString());
        crawlerProperties.setJobOutputPath(jobFile.toString());

        when(companyService.getOrCreateCompany(any())).thenReturn(testCompany());
        when(jobRepository.findBySourceAndExternalId("wanted", 456L)).thenReturn(Optional.empty());

        jobParserService.parseAndSave();

        ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(captor.capture());
        assertThat(captor.getValue().getCareerLevel()).isEqualTo("신입");
    }

    private Company testCompany() {
        return Company.builder().name("테스트 회사").build();
    }
}
