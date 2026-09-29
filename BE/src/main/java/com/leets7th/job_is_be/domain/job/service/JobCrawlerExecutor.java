package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.global.config.CrawlerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobCrawlerExecutor {

    private final CrawlerProperties crawlerProperties;
    private final JobParserService jobParserService;

    public void executePipeline() {
        log.info("파이썬 크롤링 파이프라인 실행 시작...");

        List<String> command = new ArrayList<>();
        command.add(crawlerProperties.getPythonPath());
        command.add(crawlerProperties.getScriptPath());

        command.add("--out");
        // 설정값 기반, 부모 디렉토리 경로를 동적 추출하여 대입
        String outputPath = new java.io.File(crawlerProperties.getCompanyOutputPath()).getParent();
        command.add(outputPath);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true); // 파이썬의 표준 에러도 표준 출력 채널로 합쳐서 한 번에 읽기

            Process process = processBuilder.start();

            // 파이썬 콘솔 출력 실시간으로 자바 로그에 기록
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("[Python] {}", line);
                }
            }

            // 프로세스가 완료될 때까지 대기 후 종료 코드(Exit Code) 확인
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("파이썬 크롤링 파이프라인 실행 완료 (Exit Code: 0)");
                jobParserService.parseAndSave();
            } else {
                log.error("파이썬 크롤링 파이프라인 실행 실패 (Exit Code: {})", exitCode);
                throw new RuntimeException("크롤링 파이프라인 실행 실패: Exit Code " + exitCode);
            }

        } catch (Exception e) {
            log.error("파이썬 프로세스 실행 중 예외 발생: ", e);
        }
    }
}
