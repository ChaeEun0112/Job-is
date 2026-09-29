package com.leets7th.job_is_be.domain.user.service;

import com.leets7th.job_is_be.domain.user.dto.PresignedUrlRequest;
import com.leets7th.job_is_be.domain.user.dto.PresignedUrlResponse;
import com.leets7th.job_is_be.domain.user.dto.ResumeConfirmRequest;
import com.leets7th.job_is_be.domain.user.dto.ResumeResponse;
import com.leets7th.job_is_be.domain.user.dto.ResumeUploadResponse;
import com.leets7th.job_is_be.domain.user.entity.Resume;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.enums.ResumeCategory;
import com.leets7th.job_is_be.domain.user.enums.ResumeFileFormat;
import com.leets7th.job_is_be.domain.user.enums.SocialType;
import com.leets7th.job_is_be.domain.user.repository.ResumeRepository;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.properties.AwsS3Properties;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private S3Client s3Client;
    @Mock
    private S3Presigner s3Presigner;

    private final AwsS3Properties awsS3Properties =
            new AwsS3Properties("test-bucket", "ap-northeast-2", Duration.ofMinutes(5));

    private ResumeService resumeService;

    private ResumeService newService() {
        return new ResumeService(resumeRepository, userRepository, s3Client, s3Presigner, awsS3Properties);
    }

    private User user(Long id) {
        User user = User.builder()
                .socialId("social-id")
                .socialType(SocialType.GOOGLE)
                .email("user@example.com")
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    void 파일명_확장자가_유효하면_Presigned_URL을_발급한다() throws Exception {
        resumeService = newService();

        PresignedPutObjectRequest presignedRequest = mock(PresignedPutObjectRequest.class);
        when(presignedRequest.url()).thenReturn(URI.create("https://s3.example.com/upload").toURL());
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedRequest);

        PresignedUrlRequest request = new PresignedUrlRequest(ResumeCategory.RESUME, "resume.pdf");

        PresignedUrlResponse response = resumeService.issuePresignedUrl(1L, request);

        assertThat(response.presignedUrl()).isEqualTo("https://s3.example.com/upload");
        assertThat(response.objectKey()).isEqualTo("profile/1/RESUME");
        assertThat(response.expiresIn()).isEqualTo(300L);
    }

    @Test
    void 지원하지_않는_확장자면_Presigned_URL_발급시_예외를_던진다() {
        resumeService = newService();

        PresignedUrlRequest request = new PresignedUrlRequest(ResumeCategory.RESUME, "resume.exe");

        assertThatThrownBy(() -> resumeService.issuePresignedUrl(1L, request))
                .isInstanceOf(GeneralException.class)
                .satisfies(e -> assertThat(((GeneralException) e).getErrorStatus())
                        .isEqualTo(ErrorStatus.RESUME_INVALID_FILE_FORMAT));
    }

    @Test
    void objectKey가_불일치하면_업로드_확인시_예외를_던진다() {
        resumeService = newService();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));

        ResumeConfirmRequest request = new ResumeConfirmRequest("profile/1/COVER_LETTER", "resume.pdf", ResumeCategory.RESUME);

        assertThatThrownBy(() -> resumeService.confirmUpload(1L, request))
                .isInstanceOf(GeneralException.class)
                .satisfies(e -> assertThat(((GeneralException) e).getErrorStatus())
                        .isEqualTo(ErrorStatus.RESUME_OBJECT_KEY_MISMATCH));
    }

    @Test
    void S3에_업로드된_파일이_없으면_업로드_확인시_예외를_던진다() {
        resumeService = newService();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().message("no such key").build());

        ResumeConfirmRequest request = new ResumeConfirmRequest("profile/1/RESUME", "resume.pdf", ResumeCategory.RESUME);

        assertThatThrownBy(() -> resumeService.confirmUpload(1L, request))
                .isInstanceOf(GeneralException.class)
                .satisfies(e -> assertThat(((GeneralException) e).getErrorStatus())
                        .isEqualTo(ErrorStatus.RESUME_UPLOAD_NOT_FOUND));
    }

    @Test
    void 파일_용량이_초과되면_S3_객체를_삭제하고_예외를_던진다() {
        resumeService = newService();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));

        HeadObjectResponse headObjectResponse = HeadObjectResponse.builder()
                .contentLength(11L * 1024 * 1024)
                .build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(headObjectResponse);

        ResumeConfirmRequest request = new ResumeConfirmRequest("profile/1/RESUME", "resume.pdf", ResumeCategory.RESUME);

        assertThatThrownBy(() -> resumeService.confirmUpload(1L, request))
                .isInstanceOf(GeneralException.class)
                .satisfies(e -> assertThat(((GeneralException) e).getErrorStatus())
                        .isEqualTo(ErrorStatus.RESUME_FILE_TOO_LARGE));

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void 같은_카테고리에_기존_파일이_없으면_새로_저장한다() {
        resumeService = newService();
        User user = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(resumeRepository.findByUserAndCategory(user, ResumeCategory.RESUME)).thenReturn(Optional.empty());

        HeadObjectResponse headObjectResponse = HeadObjectResponse.builder()
                .contentLength(1024L)
                .build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(headObjectResponse);

        Resume savedResume = Resume.builder()
                .user(user)
                .category(ResumeCategory.RESUME)
                .fileName("resume.pdf")
                .fileFormat(ResumeFileFormat.PDF)
                .s3Key("profile/1/RESUME")
                .uploadedAt(java.time.OffsetDateTime.now())
                .build();
        ReflectionTestUtils.setField(savedResume, "id", 10L);
        when(resumeRepository.save(any(Resume.class))).thenReturn(savedResume);

        ResumeConfirmRequest request = new ResumeConfirmRequest("profile/1/RESUME", "resume.pdf", ResumeCategory.RESUME);

        ResumeUploadResponse response = resumeService.confirmUpload(1L, request);

        assertThat(response.fileId()).isEqualTo(10L);
        assertThat(response.created()).isTrue();
        verify(resumeRepository).save(any(Resume.class));
    }

    @Test
    void 같은_카테고리에_기존_파일이_있으면_갱신한다() {
        resumeService = newService();
        User user = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Resume existing = Resume.builder()
                .user(user)
                .category(ResumeCategory.RESUME)
                .fileName("old.pdf")
                .fileFormat(ResumeFileFormat.PDF)
                .s3Key("profile/1/RESUME")
                .uploadedAt(java.time.OffsetDateTime.now().minusDays(1))
                .build();
        ReflectionTestUtils.setField(existing, "id", 5L);
        when(resumeRepository.findByUserAndCategory(user, ResumeCategory.RESUME)).thenReturn(Optional.of(existing));

        HeadObjectResponse headObjectResponse = HeadObjectResponse.builder()
                .contentLength(2048L)
                .build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(headObjectResponse);

        ResumeConfirmRequest request = new ResumeConfirmRequest("profile/1/RESUME", "new.docx", ResumeCategory.RESUME);

        ResumeUploadResponse response = resumeService.confirmUpload(1L, request);

        assertThat(response.fileId()).isEqualTo(5L);
        assertThat(response.created()).isFalse();
        assertThat(existing.getFileName()).isEqualTo("new.docx");
        assertThat(existing.getFileFormat()).isEqualTo(ResumeFileFormat.DOCX);
        verify(resumeRepository, never()).save(any(Resume.class));
    }

    @Test
    void 사용자의_파일_목록을_반환한다() {
        resumeService = newService();
        User user = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Resume resume = Resume.builder()
                .user(user)
                .category(ResumeCategory.COVER_LETTER)
                .fileName("cover.hwp")
                .fileFormat(ResumeFileFormat.HWP)
                .s3Key("profile/1/COVER_LETTER")
                .uploadedAt(java.time.OffsetDateTime.now())
                .build();
        when(resumeRepository.findAllByUser(user)).thenReturn(List.of(resume));

        List<ResumeResponse> responses = resumeService.getFiles(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).fileName()).isEqualTo("cover.hwp");
        assertThat(responses.get(0).category()).isEqualTo(ResumeCategory.COVER_LETTER);
        assertThat(responses.get(0).fileFormat()).isEqualTo(ResumeFileFormat.HWP);
    }

    @Test
    void 존재하지_않는_파일을_삭제하면_예외를_던진다() {
        resumeService = newService();
        User user = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(resumeRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resumeService.deleteFile(1L, 99L))
                .isInstanceOf(GeneralException.class)
                .satisfies(e -> assertThat(((GeneralException) e).getErrorStatus())
                        .isEqualTo(ErrorStatus.RESUME_NOT_FOUND));

        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void 파일을_삭제하면_S3_객체와_레코드를_함께_삭제한다() {
        resumeService = newService();
        User user = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Resume resume = Resume.builder()
                .user(user)
                .category(ResumeCategory.RESUME)
                .fileName("resume.pdf")
                .fileFormat(ResumeFileFormat.PDF)
                .s3Key("profile/1/RESUME")
                .uploadedAt(java.time.OffsetDateTime.now())
                .build();
        ReflectionTestUtils.setField(resume, "id", 7L);
        when(resumeRepository.findByIdAndUser(7L, user)).thenReturn(Optional.of(resume));

        resumeService.deleteFile(1L, 7L);

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
        verify(resumeRepository).delete(resume);
    }
}
