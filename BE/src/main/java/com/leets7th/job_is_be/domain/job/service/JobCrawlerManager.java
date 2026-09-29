package com.leets7th.job_is_be.domain.job.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobCrawlerManager {

    private final JobCrawlerExecutor jobCrawlerExecutor;
    private final JobParserService jobParserService;

    /**
     * 파이썬 크롤러 실행부터 DB 적재까지 전체 파이프라인을 일괄 수행.
     */
    public void runPipelineAndSave() {
        log.info("=== 전체 크롤링 및 DB 적재 파이프라인 시작 ===");

        jobCrawlerExecutor.executePipeline();
        jobParserService.parseAndSave();

        log.info("=== 전체 크롤링 및 DB 적재 파이프라인 성공적으로 종료 ===");
    }
}
