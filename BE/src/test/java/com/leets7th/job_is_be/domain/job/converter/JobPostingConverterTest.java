package com.leets7th.job_is_be.domain.job.converter;

import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.entity.JobPosting;
import com.leets7th.job_is_be.domain.job.enums.JobStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class JobPostingConverterTest {

    private final JobPostingConverter converter = new JobPostingConverter();

    @Test
    void 원문_상태가_active면_ACTIVE로_변환된다() {
        JobPosting posting = JobPosting.builder()
                .source("wanted").externalId(1L).position("백엔드 엔지니어").status("active")
                .build();

        Job job = converter.createFrom(posting);

        assertThat(job.getStatus()).isEqualTo(JobStatus.ACTIVE);
    }

    @Test
    void 마감일이_지난_공고는_EXPIRED로_변환된다() {
        JobPosting posting = JobPosting.builder()
                .source("wanted").externalId(1L).position("백엔드 엔지니어").status("active")
                .dueTime(OffsetDateTime.now().minusDays(1))
                .build();

        Job job = converter.createFrom(posting);

        assertThat(job.getStatus()).isEqualTo(JobStatus.EXPIRED);
    }

    @Test
    void 원문_상태가_active가_아니면_REMOVED로_변환된다() {
        JobPosting posting = JobPosting.builder()
                .source("wanted").externalId(1L).position("백엔드 엔지니어").status("closed")
                .build();

        Job job = converter.createFrom(posting);

        assertThat(job.getStatus()).isEqualTo(JobStatus.REMOVED);
    }

    @Test
    void 신입만_가능하고_경력범위가_없으면_신입으로_표기한다() {
        JobPosting posting = JobPosting.builder()
                .source("wanted").externalId(1L).position("백엔드 엔지니어").status("active")
                .isNewbie(true)
                .build();

        Job job = converter.createFrom(posting);

        assertThat(job.getCareerLevel()).isEqualTo("신입");
    }

    @Test
    void 경력_최소최대가_있으면_범위로_표기한다() {
        JobPosting posting = JobPosting.builder()
                .source("wanted").externalId(1L).position("백엔드 엔지니어").status("active")
                .careerMin(2).careerMax(5)
                .build();

        Job job = converter.createFrom(posting);

        assertThat(job.getCareerLevel()).isEqualTo("2~5년");
    }

    @Test
    void 경력정보가_전혀_없으면_경력무관으로_표기한다() {
        JobPosting posting = JobPosting.builder()
                .source("wanted").externalId(1L).position("백엔드 엔지니어").status("active")
                .build();

        Job job = converter.createFrom(posting);

        assertThat(job.getCareerLevel()).isEqualTo("경력무관");
    }

    @Test
    void updateFrom은_기존_Job의_필드를_최신_원문으로_갱신한다() {
        Job existing = Job.builder().source("wanted").externalId(1L).title("old").build();
        JobPosting posting = JobPosting.builder()
                .source("wanted").externalId(1L).position("new title").status("closed")
                .isRemote(true)
                .build();

        converter.updateFrom(existing, posting);

        assertThat(existing.getTitle()).isEqualTo("new title");
        assertThat(existing.getRemoteAvailable()).isTrue();
        assertThat(existing.getStatus()).isEqualTo(JobStatus.REMOVED);
    }
}
