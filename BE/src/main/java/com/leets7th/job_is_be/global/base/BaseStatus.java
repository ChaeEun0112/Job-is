package com.leets7th.job_is_be.global.base;

import org.springframework.http.HttpStatus;

public interface BaseStatus {
    HttpStatus getHttpStatus();
    String getCode();
    String getMessage();
}
