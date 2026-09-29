package com.leets7th.job_is_be.domain.user.service;

import com.leets7th.job_is_be.domain.user.dto.PresignedUrlRequest;
import com.leets7th.job_is_be.domain.user.dto.PresignedUrlResponse;
import com.leets7th.job_is_be.domain.user.dto.ResumeConfirmRequest;
import com.leets7th.job_is_be.domain.user.dto.ResumeDownloadUrlResponse;
import com.leets7th.job_is_be.domain.user.dto.ResumeResponse;
import com.leets7th.job_is_be.domain.user.dto.ResumeUploadResponse;
import com.leets7th.job_is_be.domain.user.entity.Resume;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.enums.ResumeCategory;
import com.leets7th.job_is_be.domain.user.enums.ResumeFileFormat;
import com.leets7th.job_is_be.domain.user.repository.ResumeRepository;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.properties.AwsS3Properties;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ResumeService {

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024; // 10MB

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AwsS3Properties awsS3Properties;

    public ResumeService(
            ResumeRepository resumeRepository,
            UserRepository userRepository,
            S3Client s3Client,
            S3Presigner s3Presigner,
            AwsS3Properties awsS3Properties
    ) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.awsS3Properties = awsS3Properties;
    }

    public PresignedUrlResponse issuePresignedUrl(Long userId, PresignedUrlRequest request) {
        ResumeFileFormat fileFormat = parseFileFormat(request.fileName()); // 확장자 검증 (PDF/DOCX/HWP/HWPX 아니면 예외)
        String objectKey = buildObjectKey(userId, request.category());

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(awsS3Properties.bucket())
                .key(objectKey)
                .contentType(fileFormat.mimeType()) // 클라이언트 값 대신 확장자 기준 서버 매핑 MIME 사용
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(awsS3Properties.presignedUrlExpiration())
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return new PresignedUrlResponse(
                presignedRequest.url().toString(),
                objectKey,
                awsS3Properties.presignedUrlExpiration().toSeconds()
        );
    }

    @Transactional(readOnly = true)
    public ResumeDownloadUrlResponse issueDownloadUrl(Long userId, Long fileId) {
        User user = getUser(userId);
        Resume resume = resumeRepository.findByIdAndUser(fileId, user)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESUME_NOT_FOUND));

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(awsS3Properties.bucket())
                .key(resume.getS3Key())
                .responseContentDisposition(contentDisposition(resume.getFileName()))
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(awsS3Properties.presignedUrlExpiration())
                .getObjectRequest(objectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

        return new ResumeDownloadUrlResponse(
                presignedRequest.url().toString(),
                resume.getFileName(),
                awsS3Properties.presignedUrlExpiration().toSeconds()
        );
    }

    // S3 오브젝트 키(profile/{userId}/{category})엔 확장자가 없어서, 다운로드 시 원본 파일명이 붙도록 명시적으로 지정한다.
    // URLEncoder는 '.', '-', '*', '_'를 인코딩하지 않지만 RFC 5987 attr-char엔 '*'가 없어 별도로 퍼센트 인코딩해야 한다.
    private String contentDisposition(String fileName) {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A");
        return "attachment; filename*=UTF-8''" + encoded;
    }

    @Transactional
    public ResumeUploadResponse confirmUpload(Long userId, ResumeConfirmRequest request) {
        User user = getUser(userId);
        ResumeFileFormat fileFormat = parseFileFormat(request.fileName());

        String expectedObjectKey = buildObjectKey(userId, request.category());
        if (!expectedObjectKey.equals(request.objectKey())) {
            throw new GeneralException(ErrorStatus.RESUME_OBJECT_KEY_MISMATCH);
        }

        HeadObjectResponse headObjectResponse = headObjectOrThrow(request.objectKey());
        if (headObjectResponse.contentLength() > MAX_FILE_SIZE_BYTES) {
            deleteObject(request.objectKey());
            throw new GeneralException(ErrorStatus.RESUME_FILE_TOO_LARGE);
        }

        OffsetDateTime uploadedAt = OffsetDateTime.now();
        Optional<Resume> existingResume = resumeRepository.findByUserAndCategory(user, request.category());
        boolean created = existingResume.isEmpty();

        Resume resume = existingResume
                .map(existing -> {
                    existing.replace(request.fileName(), fileFormat, uploadedAt);
                    return existing;
                })
                .orElseGet(() -> resumeRepository.save(Resume.builder()
                        .user(user)
                        .category(request.category())
                        .fileName(request.fileName())
                        .fileFormat(fileFormat)
                        .s3Key(request.objectKey())
                        .uploadedAt(uploadedAt)
                        .build()));

        return new ResumeUploadResponse(resume.getId(), created);
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> getFiles(Long userId) {
        User user = getUser(userId);
        return resumeRepository.findAllByUser(user).stream()
                .map(ResumeResponse::from)
                .toList();
    }

    @Transactional
    public void deleteFile(Long userId, Long fileId) {
        User user = getUser(userId);
        Resume resume = resumeRepository.findByIdAndUser(fileId, user)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESUME_NOT_FOUND));

        String s3Key = resume.getS3Key();
        resumeRepository.delete(resume);

        // DB 커밋이 실패하면 S3 삭제도 실행되지 않도록, 커밋 성공 이후에만 S3 객체를 삭제한다.
        // (S3 orphan은 무해하지만, S3만 먼저 지우고 DB 커밋이 실패하면 ghost row가 남는다)
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    deleteObject(s3Key);
                }
            });
        } else {
            deleteObject(s3Key);
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
    }

    private ResumeFileFormat parseFileFormat(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) {
            throw new GeneralException(ErrorStatus.RESUME_INVALID_FILE_FORMAT);
        }

        try {
            return ResumeFileFormat.fromExtension(fileName.substring(lastDot + 1));
        } catch (IllegalArgumentException e) {
            throw new GeneralException(ErrorStatus.RESUME_INVALID_FILE_FORMAT);
        }
    }

    private String buildObjectKey(Long userId, ResumeCategory category) {
        return "profile/" + userId + "/" + category.name();
    }

    private HeadObjectResponse headObjectOrThrow(String objectKey) {
        try {
            return s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(awsS3Properties.bucket())
                    .key(objectKey)
                    .build());
        } catch (NoSuchKeyException e) {
            throw new GeneralException(ErrorStatus.RESUME_UPLOAD_NOT_FOUND);
        } catch (S3Exception e) {
            // HeadObject 404는 body가 없어 SDK 버전에 따라 NoSuchKeyException 대신 S3Exception(404)로 올 수 있음
            if (e.statusCode() == 404) {
                throw new GeneralException(ErrorStatus.RESUME_UPLOAD_NOT_FOUND);
            }
            throw e;
        }
    }

    private void deleteObject(String objectKey) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(awsS3Properties.bucket())
                .key(objectKey)
                .build());
    }
}
