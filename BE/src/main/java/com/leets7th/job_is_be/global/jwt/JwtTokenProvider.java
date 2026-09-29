package com.leets7th.job_is_be.global.jwt;

import com.leets7th.job_is_be.global.properties.JwtProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder tokenDecoder;
    private final JwtProperties properties;
    private final Clock clock;

    @Autowired
    public JwtTokenProvider(
            JwtEncoder jwtEncoder,
            @Qualifier("tokenDecoder") JwtDecoder tokenDecoder,
            JwtProperties properties
    ) {
        this(jwtEncoder, tokenDecoder, properties, Clock.systemUTC());
    }

    JwtTokenProvider(
            JwtEncoder jwtEncoder,
            JwtDecoder tokenDecoder,
            JwtProperties properties,
            Clock clock
    ) {
        this.jwtEncoder = jwtEncoder;
        this.tokenDecoder = tokenDecoder;
        this.properties = properties;
        this.clock = clock;
    }

    public TokenPair issueTokenPair(Long userId, String role) {
        Instant issuedAt = clock.instant();
        String refreshSessionId = UUID.randomUUID().toString();

        String accessToken = encode(
                userId,
                UUID.randomUUID().toString(),
                ACCESS_TOKEN_TYPE,
                issuedAt,
                properties.accessTokenExpiration(),
                role
        );
        String refreshToken = encode(
                userId,
                refreshSessionId,
                REFRESH_TOKEN_TYPE,
                issuedAt,
                properties.refreshTokenExpiration(),
                role
        );

        return new TokenPair(
                accessToken,
                refreshToken,
                refreshSessionId,
                properties.accessTokenExpiration().toSeconds(),
                properties.refreshTokenExpiration()
        );
    }

    public RefreshTokenClaims decodeRefreshToken(String refreshToken) {
        Jwt jwt = tokenDecoder.decode(refreshToken);
        if (!REFRESH_TOKEN_TYPE.equals(jwt.getClaimAsString("tokenType"))) {
            throw new IllegalArgumentException("Unexpected JWT token type");
        }

        return new RefreshTokenClaims(
                Long.valueOf(jwt.getSubject()),
                jwt.getId()
        );
    }

    private String encode(
            Long userId,
            String tokenId,
            String tokenType,
            Instant issuedAt,
            Duration expiration,
            String role
    ) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(userId.toString())
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(expiration))
                .id(tokenId)
                .claim("tokenType", tokenType)
                .claim("role", role)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public record TokenPair(
            String accessToken,
            String refreshToken,
            String refreshSessionId,
            long accessTokenExpiresIn,
            Duration refreshTokenTtl
    ) {
    }

    public record RefreshTokenClaims(Long userId, String sessionId) {
    }
}
