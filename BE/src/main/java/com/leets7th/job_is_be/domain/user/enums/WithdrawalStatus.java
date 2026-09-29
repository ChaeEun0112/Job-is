package com.leets7th.job_is_be.domain.user.enums;

public enum WithdrawalStatus {
    PENDING,    // 30일 유예기간 중
    COMPLETED,  // 유예기간 경과, 완전 삭제됨
    RESTORED    // 유예기간 내 복구됨
}
