package com.leets7th.job_is_be.domain.job.repository;

import com.leets7th.job_is_be.domain.job.dto.JobSearchRequest;
import com.leets7th.job_is_be.domain.job.dto.JobSummaryResponse;
import com.leets7th.job_is_be.domain.job.entity.Job;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 검색 필터의 cities 파라미터가 실제로 job.locationCity를 걸러내는지 검증한다
 * (SearchJobsParams.cities <-> JobSearchRequest.cities 이름 불일치로 인해
 * 지금까지 프론트에서 지역 필터가 조용히 무시되고 있었음).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JobRepositoryCustomImplTest {

    @Autowired
    private JobRepository jobRepository;

    @Test
    void cities_필터로_해당_지역_공고만_조회된다() {
        jobRepository.save(activeJob("서울 공고", "wanted", 1L, "서울"));
        jobRepository.save(activeJob("부산 공고", "wanted", 2L, "부산"));

        JobSearchRequest request = JobSearchRequest.builder()
                .cities(List.of("서울"))
                .build();

        Page<JobSummaryResponse> result = jobRepository.searchJobs(request, PageRequest.of(0, 10), Map.of());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).position()).isEqualTo("서울 공고");
    }

    private Job activeJob(String title, String source, Long externalId, String locationCity) {
        Job job = Job.builder()
                .title(title)
                .source(source)
                .externalId(externalId)
                .locationCity(locationCity)
                .build();
        return job;
    }
}
