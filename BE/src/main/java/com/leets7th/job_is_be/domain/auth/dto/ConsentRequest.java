package com.leets7th.job_is_be.domain.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record ConsentRequest(
        @NotNull @AssertTrue Boolean termsAgreed,
        @NotNull @AssertTrue Boolean privacyAgreed,
        @NotNull @AssertTrue Boolean ageOver14Agreed,
        Boolean marketingAgreed
) {
}
