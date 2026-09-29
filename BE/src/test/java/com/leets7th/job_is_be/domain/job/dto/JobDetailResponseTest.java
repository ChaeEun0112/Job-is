package com.leets7th.job_is_be.domain.job.dto;

import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class JobDetailResponseTest {

    @Test
    void from은_editorNote_postedAt_status를_노출한다() {
        OffsetDateTime postedAt = OffsetDateTime.parse("2026-08-01T00:00:00+09:00");
        Job job = Job.builder()
                .title("백엔드 엔지니어")
                .postedAt(postedAt)
                .editorNote("에디터 코멘트")
                .build();

        JobDetailResponse response = JobDetailResponse.from(job);

        assertThat(response.postedAt()).isEqualTo(postedAt);
        assertThat(response.status()).isEqualTo(JobStatus.ACTIVE);
        assertThat(response.editorNote()).isEqualTo("에디터 코멘트");
    }
}
