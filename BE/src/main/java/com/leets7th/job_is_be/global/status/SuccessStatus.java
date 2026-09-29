package com.leets7th.job_is_be.global.status;

import com.leets7th.job_is_be.global.base.BaseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseStatus {

    // 예시
    COMM_SUCCESS_STATUS(HttpStatus.OK, "COMM_200", "성공적으로 처리되었습니다."),

    /**
     * Auth
     */
    TOKEN_REISSUE_SUCCESS(HttpStatus.OK, "AUTH_200_1", "토큰을 재발급했습니다."),
    SESSION_GET_SUCCESS(HttpStatus.OK, "AUTH_200_2", "세션 상태를 조회했습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "AUTH_200_3", "로그아웃했습니다."),
    CSRF_TOKEN_GET_SUCCESS(HttpStatus.OK, "AUTH_200_4", "CSRF 토큰을 발급했습니다."),
    DEV_LOGIN_SUCCESS(HttpStatus.OK, "AUTH_200_5", "개발자 전용 로그인에 성공했습니다."),
    OAUTH_EXCHANGE_SUCCESS(HttpStatus.OK, "AUTH_200_6", "로그인 토큰 교환에 성공했습니다."),
    CONSENT_SAVE_SUCCESS(HttpStatus.OK, "AUTH_200_7", "필수 약관 동의를 저장했습니다."),
    WITHDRAWAL_SUCCESS(HttpStatus.OK, "AUTH_200_8", "회원 탈퇴를 요청했습니다."),
    WITHDRAWAL_RESTORE_SUCCESS(HttpStatus.OK, "AUTH_200_9", "회원 탈퇴를 복구했습니다."),

    /**
     * Profile
     */
    QUIZ_QUESTIONS_GET_SUCCESS(HttpStatus.OK, "QUIZ_200_1", "직무 성향 퀴즈 문항을 조회했습니다."),
    QUIZ_ANSWER_SAVE_SUCCESS(HttpStatus.OK, "QUIZ_200_2", "퀴즈 응답을 저장했습니다."),
    QUIZ_RESULT_GET_SUCCESS(HttpStatus.OK, "QUIZ_200_3", "직무 성향 퀴즈 결과를 조회했습니다."),
    QUIZ_RESULT_APPLY_SUCCESS(HttpStatus.OK, "QUIZ_200_4", "퀴즈 결과를 프로필에 반영했습니다."),

    PROFILE_GET_SUCCESS(HttpStatus.OK, "PROFILE_200_1", "프로필을 조회했습니다."),
    PROFILE_UPDATE_SUCCESS(HttpStatus.OK, "PROFILE_200_2", "프로필을 수정했습니다."),
    PROFILE_DRAFT_GET_SUCCESS(HttpStatus.OK, "PROFILE_200_3", "온보딩 임시저장을 조회했습니다."),
    PROFILE_DRAFT_SAVE_SUCCESS(HttpStatus.OK, "PROFILE_200_4", "온보딩 내용을 임시저장했습니다."),
    PROFILE_ONBOARDING_COMPLETE_SUCCESS(HttpStatus.OK, "PROFILE_200_5", "온보딩을 완료했습니다."),


    /**
     * Deck
     */
    DECK_GENERATE_SUCCESS(HttpStatus.OK, "DECK_200_2", "오늘의 추천 덱을 생성했습니다."),
    CARD_DISMISS_SUCCESS(HttpStatus.OK, "DECK_200_3", "카드를 관심없음으로 처리했습니다."),
    CARD_DISMISS_CANCEL_SUCCESS(HttpStatus.OK, "DECK_200_4", "카드의 관심없음 처리를 취소했습니다."),
    CARD_DISMISS_REASON_SUCCESS(HttpStatus.OK, "DECK_200_5", "관심없음 사유를 제출했습니다."),

    /**
     * Briefing
     */
    BRIEFING_TODAY_SUCCESS(HttpStatus.OK, "BRIEFING_200", "오늘의 브리핑을 조회했습니다."),
    BRIEFING_STATUS_SUCCESS(HttpStatus.OK, "BRIEFING_200_2", "오늘의 브리핑 덱 카드 목록을 조회했습니다."),

    /**
     * Resume (이력서/자소서 파일)
     */
    RESUME_PRESIGNED_URL_SUCCESS(HttpStatus.OK, "RESUME_200_1", "업로드용 Presigned URL을 발급했습니다."),
    RESUME_UPLOAD_CONFIRM_SUCCESS(HttpStatus.CREATED, "RESUME_201_1", "이력서/자소서 파일을 저장했습니다."),
    RESUME_LIST_SUCCESS(HttpStatus.OK, "RESUME_200_2", "이력서/자소서 파일 목록을 조회했습니다."),
    RESUME_DELETE_SUCCESS(HttpStatus.OK, "RESUME_200_3", "이력서/자소서 파일을 삭제했습니다."),
    RESUME_UPLOAD_UPDATE_SUCCESS(HttpStatus.OK, "RESUME_200_4", "이력서/자소서 파일을 갱신했습니다."),
    RESUME_DOWNLOAD_URL_SUCCESS(HttpStatus.OK, "RESUME_200_5", "다운로드용 Presigned URL을 발급했습니다."),

    /**
     * Job
     */
    JOB_SAVE_SUCCESS(HttpStatus.CREATED, "JOB_201_1", "공고를 저장했습니다."),
    JOB_UNSAVE_SUCCESS(HttpStatus.OK, "JOB_200_1", "공고 저장을 취소했습니다."),
    JOB_VIEW_RECORD_SUCCESS(HttpStatus.OK, "JOB_200_2", "공고 열람을 기록했습니다."),
    JOB_APPLY_INTENT_TOGGLE_SUCCESS(HttpStatus.OK, "JOB_200_3", "지원 의향 상태를 변경했습니다."),
    JOB_APPLY_RECORD_SUCCESS(HttpStatus.OK, "JOB_200_4", "지원하기 클릭을 기록했습니다."),
    SAVE_LIST_GET_SUCCESS(HttpStatus.OK, "JOB_200_5", "저장 목록을 조회했습니다."),
    JOB_SEARCH_SUCCESS(HttpStatus.OK, "JOB_200_6", "공고 목록을 조회했습니다."),
    JOB_DETAIL_SUCCESS(HttpStatus.OK, "JOB_200_7", "공고 상세 정보를 조회했습니다."),
    JOB_SIMILAR_SUCCESS(HttpStatus.OK, "JOB_200_8", "유사 추천 공고를 조회했습니다."),
    JOB_SOURCE_CHECK_SUCCESS(HttpStatus.OK, "JOB_200_9", "원문 링크 유효성을 확인했습니다."),
    JOB_SKIP_SUCCESS(HttpStatus.OK, "JOB_200_10", "공고를 관심 없음으로 표시했습니다."),
    JOB_SKIP_UNDO_SUCCESS(HttpStatus.OK, "JOB_200_11", "관심 없음 표시를 해제했습니다."),

    /**
     * Metadata / TechStack
     */
    TECH_STACK_GET_SUCCESS(HttpStatus.OK, "TECH_200_1", "기술 스택 목록을 조회했습니다."),
    REGION_GET_SUCCESS(HttpStatus.OK, "METADATA_200_1", "지역 목록을 조회했습니다."),
    JOB_CATEGORY_GET_SUCCESS(HttpStatus.OK, "METADATA_200_2", "직무 카테고리 목록을 조회했습니다."),
    CAREER_LEVEL_GET_SUCCESS(HttpStatus.OK, "METADATA_200_3", "경력 수준 목록을 조회했습니다."),
    EMPLOYMENT_TYPE_GET_SUCCESS(HttpStatus.OK, "METADATA_200_4", "고용형태 목록을 조회했습니다."),

    /**
     * Crawler (Admin)
     */
    CRAWLER_PIPELINE_RUN_SUCCESS(HttpStatus.OK, "CRAWLER_200_1", "크롤링 및 DB 적재 파이프라인을 실행했습니다."),
    JOB_POSTING_SYNC_SUCCESS(HttpStatus.OK, "CRAWLER_200_2", "job_postings → jobs 동기화가 완료되었습니다."),
    JOB_POSTING_SYNC_ACCEPTED(HttpStatus.ACCEPTED, "CRAWLER_202_1", "동기화 작업이 시작되었습니다."),
    JOB_POSTING_SYNC_STATUS_SUCCESS(HttpStatus.OK, "CRAWLER_200_3", "동기화 작업 상태를 조회했습니다."),

    /**
     * History
     */
    HISTORY_LIST_GET_SUCCESS(HttpStatus.OK, "HISTORY_200_1", "열람·스킵·저장·지원 의향 내역을 조회했습니다."),

    /**
     * Content
     */
    CONTENT_LIST_GET_SUCCESS(HttpStatus.OK, "CONTENT_200_1", "오늘의 소식 목록을 조회했습니다."),
    CONTENT_DETAIL_GET_SUCCESS(HttpStatus.OK, "CONTENT_200_2", "뉴스/혜택 상세를 조회했습니다."),

    /**
     * Notification
     */
    NOTIFICATION_SETTING_GET_SUCCESS(HttpStatus.OK, "NOTIFICATION_200_1", "알림 수신 설정을 조회했습니다."),
    NOTIFICATION_SETTING_UPDATE_SUCCESS(HttpStatus.OK, "NOTIFICATION_200_2", "알림 수신 설정을 변경했습니다."),
    NOTIFICATION_SNOOZE_SUCCESS(HttpStatus.OK, "NOTIFICATION_200_3", "알림 발송을 스누즈 설정했습니다."),
    NOTIFICATION_SNOOZE_CANCEL_SUCCESS(HttpStatus.OK, "NOTIFICATION_200_4", "알림 스누즈를 해제했습니다."),

    /**
     * Account
     */
    ACCOUNT_GET_SUCCESS(HttpStatus.OK, "ACCOUNT_200_1", "계정 정보를 조회했습니다."),

    /**
     * Unsubscribe
     */
    UNSUBSCRIBE_SUCCESS(HttpStatus.OK, "UNSUBSCRIBE_200_1", "수신거부가 처리되었습니다."),
    RESUBSCRIBE_SUCCESS(HttpStatus.OK, "UNSUBSCRIBE_200_2", "다시 구독되었습니다."),
    UNSUBSCRIBE_FEEDBACK_SUCCESS(HttpStatus.OK, "UNSUBSCRIBE_200_3", "해지 사유가 제출되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
