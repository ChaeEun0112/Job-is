package com.leets7th.job_is_be.global.exception;

import com.leets7th.job_is_be.global.base.BaseStatus;
import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException {

    private final BaseStatus errorStatus;

    public GeneralException(
            BaseStatus errorStatus
    ) {
        super(errorStatus.getMessage());
        this.errorStatus = errorStatus;
    }

}
