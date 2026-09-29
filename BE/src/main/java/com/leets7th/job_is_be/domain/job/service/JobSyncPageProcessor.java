package com.leets7th.job_is_be.domain.job.service;

import com.leets7th.job_is_be.domain.job.converter.JobPostingConverter;
import com.leets7th.job_is_be.domain.job.entity.JobPosting;
import com.leets7th.job_is_be.domain.job.repository.JobPostingRepository;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class JobSyncPageProcessor {

    static final int PAGE_SIZE = 200;

    private final JobPostingRepository jobPostingRepository;
    private final JobRepository jobRepository;
    private final JobPostingConverter converter;

    @Transactional
    public boolean processPage(int page) {
        Slice<JobPosting> chunk = jobPostingRepository.findAll(PageRequest.of(page, PAGE_SIZE));
        for (JobPosting posting : chunk) {
            jobRepository.findBySourceAndExternalId(posting.getSource(), posting.getExternalId())
                    .ifPresentOrElse(
                            existing -> converter.updateFrom(existing, posting),
                            () -> jobRepository.save(converter.createFrom(posting))
                    );
        }
        return chunk.hasNext();
    }
}
