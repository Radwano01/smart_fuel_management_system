package com.example.smart_fuel_management_system.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    private String secret;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        byte[] keyBytes = new byte[32];

        for (int i = 0; i < keyBytes.length; i++) {
            keyBytes[i] = (byte) i;
        }

        secret = Base64.getEncoder().encodeToString(keyBytes);

        ReflectionTestUtils.setField(
                jwtService,
                "jwtSecret",
                secret
        );
    }

    @Test
    void generateServiceToken_shouldGenerateValidToken() {
        // given
        String serviceName = "fuel-session-service";
        String audience = "payment-service";
        String scope = "payment.internal.preauth";

        // when
        String token = jwtService.generateServiceToken(
                serviceName,
                audience,
                scope
        );

        // then
        assertThat(token).isNotBlank();
        assertThat(jwtService.validateToken(token)).isTrue();
    }

    @Test
    void generateServiceToken_shouldContainExpectedClaims() {
        // given
        String serviceName = "fuel-session-service";
        String audience = "payment-service";
        String scope = "payment.internal.preauth";

        // when
        String token = jwtService.generateServiceToken(
                serviceName,
                audience,
                scope
        );

        Claims claims = jwtService.extractClaims(token);

        // then
        assertThat(claims.getIssuer())
                .isEqualTo("payment-service");

        assertThat(claims.getSubject())
                .isEqualTo(serviceName);

        assertThat(claims.get("role", String.class))
                .isEqualTo("SERVICE");

        assertThat(claims.get("scope", String.class))
                .isEqualTo(scope);

        assertThat(claims.getAudience())
                .isEqualTo(audience);

        assertThat(claims.getIssuedAt())
                .isNotNull();

        assertThat(claims.getExpiration())
                .isNotNull();
    }

    @Test
    void extractRole_shouldReturnRoleFromToken() {
        // given
        String token = jwtService.generateServiceToken(
                "fuel-session-service",
                "payment-service",
                "payment.internal.preauth"
        );

        // when
        String role = jwtService.extractRole(token);

        // then
        assertThat(role).isEqualTo("SERVICE");
    }

    @Test
    void extractIssuer_shouldReturnIssuerFromToken() {
        // given
        String token = jwtService.generateServiceToken(
                "fuel-session-service",
                "payment-service",
                "payment.internal.preauth"
        );

        // when
        String issuer = jwtService.extractIssuer(token);

        // then
        assertThat(issuer).isEqualTo("payment-service");
    }

    @Test
    void extractAudience_shouldReturnAudienceFromToken() {
        // given
        String token = jwtService.generateServiceToken(
                "fuel-session-service",
                "payment-service",
                "payment.internal.preauth"
        );

        // when
        String audience = jwtService.extractAudience(token);

        // then
        assertThat(audience).isEqualTo("payment-service");
    }

    @Test
    void extractSubject_shouldReturnSubjectFromToken() {
        // given
        String token = jwtService.generateServiceToken(
                "fuel-session-service",
                "payment-service",
                "payment.internal.preauth"
        );

        // when
        String subject = jwtService.extractSubject(token);

        // then
        assertThat(subject).isEqualTo("fuel-session-service");
    }

    @Test
    void extractUserId_shouldReturnUserId_whenTokenContainsValidUuid() {
        // given
        UUID userId = UUID.randomUUID();

        String token = Jwts.builder()
                .setSubject(userId.toString())
                .signWith(
                        Keys.hmacShaKeyFor(
                                Base64.getDecoder().decode(secret)
                        )
                )
                .compact();

        // when
        UUID result = jwtService.extractUserId(token);

        // then
        assertThat(result).isEqualTo(userId);
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenIsInvalid() {
        // given
        String token = "invalid-token";

        // when
        boolean result = jwtService.validateToken(token);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenIsSignedWithDifferentKey() {
        // given
        byte[] differentKey = new byte[32];

        for (int i = 0; i < differentKey.length; i++) {
            differentKey[i] = (byte) (i + 1);
        }

        String token = Jwts.builder()
                .setSubject(UUID.randomUUID().toString())
                .signWith(Keys.hmacShaKeyFor(differentKey))
                .compact();

        // when
        boolean result = jwtService.validateToken(token);

        // then
        assertThat(result).isFalse();
    }
}
