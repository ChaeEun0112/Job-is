package com.leets7th.job_is_be.domain.user.enums;

/**
 * 업로드 허용 파일 형식 (확장자 기준)
 */
public enum ResumeFileFormat {
    PDF("application/pdf"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    HWP("application/x-hwp"),
    HWPX("application/haansofthwpx");

    private final String mimeType;

    ResumeFileFormat(String mimeType) {
        this.mimeType = mimeType;
    }

    public static ResumeFileFormat fromExtension(String extension) {
        return valueOf(extension.toUpperCase());
    }

    /**
     * 확장자에 대응하는 서버 허용 MIME 타입.
     * 클라이언트가 보낸 contentType은 신뢰하지 않고 이 값을 S3 PutObject에 사용한다.
     */
    public String mimeType() {
        return mimeType;
    }
}
