package com.leets7th.job_is_be.domain.user.dto;

import com.leets7th.job_is_be.domain.user.entity.Resume;
import com.leets7th.job_is_be.domain.user.enums.ResumeCategory;
import com.leets7th.job_is_be.domain.user.enums.ResumeFileFormat;

import java.time.OffsetDateTime;

public record ResumeResponse(
        Long fileId,
        ResumeCategory category,
        String fileName,
        ResumeFileFormat fileFormat,
        OffsetDateTime uploadedAt
) {
    public static ResumeResponse from(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getCategory(),
                resume.getFileName(),
                resume.getFileFormat(),
                resume.getUploadedAt()
        );
    }
}
