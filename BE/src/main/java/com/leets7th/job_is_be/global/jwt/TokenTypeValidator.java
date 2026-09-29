package com.leets7th.job_is_be.global.jwt;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class TokenTypeValidator implements OAuth2TokenValidator<Jwt> {

    private final String expectedTokenType;

    public TokenTypeValidator(String expectedTokenType) {
        this.expectedTokenType = expectedTokenType;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        if (expectedTokenType.equals(token.getClaimAsString("tokenType"))) {
            return OAuth2TokenValidatorResult.success();
        }

        OAuth2Error error = new OAuth2Error(
                "invalid_token",
                "Unexpected JWT token type",
                null
        );
        return OAuth2TokenValidatorResult.failure(error);
    }
}
