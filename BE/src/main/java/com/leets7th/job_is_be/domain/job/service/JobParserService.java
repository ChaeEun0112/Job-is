package com.leets7th.job_is_be.domain.job.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets7th.job_is_be.domain.job.dto.CrawledJobDto;
import com.leets7th.job_is_be.domain.job.dto.CrawledCompanyDto;
import com.leets7th.job_is_be.domain.job.entity.Company;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.entity.JobCategory;
import com.leets7th.job_is_be.domain.job.entity.Region;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import com.leets7th.job_is_be.domain.job.repository.JobCategoryRepository;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.domain.job.repository.RegionRepository;
import com.leets7th.job_is_be.global.config.CrawlerProperties;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class JobParserService {

    // 크롤러 원문(원티드)이 내려주는 confirm_time/due_time은 오프셋이 없어 KST로 고정 해석한다.
    private static final ZoneOffset KST = ZoneOffset.ofHours(9);

    private final CrawlerProperties crawlerProperties;
    private final CompanyService companyService;
    private final JobCategoryRepository jobCategoryRepository;
    private final RegionRepository regionRepository;
    private final JobRepository jobRepository;
    private final ObjectMapper objectMapper;

    public JobParserService(CrawlerProperties crawlerProperties, CompanyService companyService,
                            JobCategoryRepository jobCategoryRepository, RegionRepository regionRepository,
                            JobRepository jobRepository, ObjectMapper objectMapper) {
        this.crawlerProperties = crawlerProperties;
        this.companyService = companyService;
        this.jobCategoryRepository = jobCategoryRepository;
        this.regionRepository = regionRepository;
        this.jobRepository = jobRepository;
        this.objectMapper = objectMapper;
    }

    public void parseAndSave() {
        log.info("크롤링 결과 파싱 및 DB 적재 시작...");

        processFile(crawlerProperties.getCompanyOutputPath(), "회사", (line) -> {
            CrawledCompanyDto companyDto = objectMapper.readValue(line, CrawledCompanyDto.class);
            companyService.getOrCreateCompany(companyDto);
        });

        Map<String, JobCategory> categoryByName = jobCategoryRepository.findAll().stream()
                .collect(Collectors.toMap(JobCategory::getName, category -> category));

        processFile(crawlerProperties.getJobOutputPath(), "공고", (line) -> {
            CrawledJobDto jobDto = objectMapper.readValue(line, CrawledJobDto.class);

            //title이 null이거나 빈 값인 경우 스킵
            if (jobDto.title() == null || jobDto.title().isBlank()) {
                log.warn("공고 제목(title)이 누락되어 저장을 건너뜁니다. (externalId: {})", jobDto.externalId());
                return;
            }

            Company company = companyService.getOrCreateCompany(jobDto.company());
            JobCategory jobCategory = findMatchingCategory(categoryByName, jobDto.categoryChild());
            Region region = regionRepository.findByName(jobDto.regionName()).orElse(null);
            OffsetDateTime postedAt = toOffsetDateTime(jobDto.postedAt());
            OffsetDateTime deadlineAt = toOffsetDateTime(jobDto.deadlineAt());

            java.util.Optional<Job> existingJob = jobRepository.findBySourceAndExternalId(jobDto.source(), jobDto.externalId());

            Job job;
            if (existingJob.isPresent()) {
                job = existingJob.get();
                job.syncFrom(
                        company,
                        jobDto.title(),
                        jobDto.careerLevel(),
                        jobDto.employmentType(),
                        jobDto.remoteAvailable(),
                        jobDto.detailUrl(),
                        postedAt,
                        deadlineAt,
                        JobStatus.ACTIVE,
                        jobDto.locationFull(),
                        jobDto.mainTasks(),
                        jobDto.requirements(),
                        jobDto.preferredPoints(),
                        jobDto.skills(),
                        false,
                        null,  // embedding은 별도 배치로 계산
                        jobDto.categoryChild(),
                        jobDto.locationCity(),
                        jobDto.locationDistrict(),
                        jobDto.isNewbie(),
                        jobDto.thumbnailUrl()
                );
            } else {
                job = Job.builder()
                        .title(jobDto.title())
                        .careerLevel(jobDto.careerLevel())
                        .employmentType(jobDto.employmentType())
                        .remoteAvailable(jobDto.remoteAvailable())
                        .sourceUrl(jobDto.detailUrl())
                        .postedAt(postedAt)
                        .deadlineAt(deadlineAt)
                        .rewardTotal(jobDto.reward())
                        .company(company)
                        .intro(jobDto.intro())
                        .mainTasks(jobDto.mainTasks())
                        .requirements(jobDto.requirements())
                        .preferredPoints(jobDto.preferredPoints())
                        .benefits(jobDto.benefits())
                        .careerMin(jobDto.careerMin())
                        .careerMax(jobDto.careerMax())
                        .jobCategory(jobCategory)
                        .region(region)
                        .source(jobDto.source())
                        .externalId(jobDto.externalId())
                        .skillTags(jobDto.skills())
                        .skillsInferred(false)
                        .categories(jobDto.categoryChild() != null ? String.join(",", jobDto.categoryChild()) : null)
                        .categoryChild(jobDto.categoryChild())
                        .locationCity(jobDto.locationCity())
                        .locationDistrict(jobDto.locationDistrict())
                        .locationFull(jobDto.locationFull())
                        .isNewbie(jobDto.isNewbie())
                        .thumbnailUrl(jobDto.thumbnailUrl())
                        .build();
            }

            jobRepository.save(job);
        });

        log.info("모든 파이프라인 데이터 적재 완료");
    }

    private JobCategory findMatchingCategory(Map<String, JobCategory> categoryByName, List<String> categoryChild) {
        if (categoryChild == null) {
            return null;
        }
        return categoryChild.stream()
                .map(categoryByName::get)
                .filter(category -> category != null)
                .findFirst()
                .orElse(null);
    }

    private OffsetDateTime toOffsetDateTime(LocalDate date) {
        return date != null ? date.atStartOfDay().atOffset(KST) : null;
    }

    private OffsetDateTime toOffsetDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.atOffset(KST) : null;
    }

    private void processFile(String path, String type, CheckedConsumer processor) {
        if (path == null) {
            log.error("{} 경로가 설정되지 않았습니다 (null).", type);
            return;
        }
        File file = new File(path);
        if (!file.exists()) {
            log.error("{} 파일이 존재하지 않습니다: {}", type, path);
            return;
        }

        int lineNumber = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // 빈 줄 제외
                if (line.trim().isEmpty()) {
                    continue;
                }

                processor.accept(line);
            }
        } catch (Exception e) {
            log.error("{} 처리 중 오류 발생 [라인 번호: {}] - 원인: {}", type, lineNumber, e.getMessage(), e);
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @FunctionalInterface
    interface CheckedConsumer { void accept(String line) throws Exception; }
}
