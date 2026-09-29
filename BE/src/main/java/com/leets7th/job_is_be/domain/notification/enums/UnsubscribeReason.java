package com.leets7th.job_is_be.domain.notification.enums;

public enum UnsubscribeReason {
    NOT_RELEVANT,       // 관련 공고가 없어요
    TOO_FREQUENT,       // 메일이 너무 자주 와요
    NOT_USING_SERVICE,  // 서비스를 더 이상 사용하지 않아요
    OTHER               // 기타 (comment에 자유 입력)
}
