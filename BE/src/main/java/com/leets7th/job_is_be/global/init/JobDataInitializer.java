package com.leets7th.job_is_be.global.init;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.leets7th.job_is_be.domain.job.entity.Company;
import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.repository.CompanyRepository;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * database/data 의 JSONL 시드 데이터를 기동 시 1회 적재한다.
 * 시드 파일은 저장소에 포함되지 않아 배포 환경에는 없으므로, 기본적으로 비활성이며
 * app.data-init.enabled=true 를 준 환경(주로 로컬)에서만 빈으로 등록된다.
 */
@Slf4j
// @Component
@ConditionalOnProperty(name = "app.data-init.enabled", havingValue = "true")
@RequiredArgsConstructor
public class JobDataInitializer implements CommandLineRunner {

    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        ObjectMapper localObjectMapper = objectMapper.copy()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

        loadCompanies("database/data/companies.jsonl", localObjectMapper);
        loadJobs("database/data/job_postings.jsonl", localObjectMapper);
    }

    private void loadCompanies(String filePath, ObjectMapper mapper) {
        File file = new File(filePath);
        if (!file.exists()) {
            log.error("초기 회사 데이터 파일이 존재하지 않습니다. 경로: {}", file.getAbsolutePath());
            throw new GeneralException(ErrorStatus.INITIAL_DATA_FILE_NOT_FOUND);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;

                try {
                    Company company = mapper.readValue(line, Company.class);

                    if (company.getId() != null && companyRepository.existsById(company.getId())) {
                        continue;
                    }
                    companyRepository.save(company);
                } catch (DataIntegrityViolationException e) {
                    log.info("동시 적재로 인한 중복 회사 데이터 건너뜀 (행 번호: {})", lineNumber);
                } catch (Exception e) {
                    log.warn("회사 데이터 {}번째 행 적재 실패 (건너뜀): {}", lineNumber, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("회사 데이터 적재 중 오류 발생: {}", e.getMessage(), e);
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void loadJobs(String filePath, ObjectMapper mapper) {
        File file = new File(filePath);
        if (!file.exists()) {
            log.error("초기 공고 데이터 파일이 존재하지 않습니다. 경로: {}", file.getAbsolutePath());
            throw new GeneralException(ErrorStatus.INITIAL_DATA_FILE_NOT_FOUND);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;

                try {
                    Job job = mapper.readValue(line, Job.class);

                    if (job.getTitle() == null || job.getTitle().isBlank()
                            || job.getSource() == null || job.getExternalId() == null) {
                        continue;
                    }

                    if (!jobRepository.existsBySourceAndExternalId(job.getSource(), job.getExternalId())) {
                        jobRepository.save(job);
                    }
                } catch (DataIntegrityViolationException e) {
                    log.info("동시 적재로 인한 중복 공고 데이터 건너뜀 (행 번호: {})", lineNumber);
                } catch (Exception e) {
                    log.warn("공고 데이터 {}번째 행 적재 실패 (건너뜀): {}", lineNumber, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("공고 데이터 적재 중 오류 발생: {}", e.getMessage(), e);
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
