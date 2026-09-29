package com.example.smart_fuel_management_system.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JWTGeneratorTest {

    private JWTGenerator jwtGenerator;

    private final String secret =
            "VGhpc0lzQVN1ZmZpY2llbnRseUxvbmdTZWNyZXRLZXlGb3JUZXN0aW5nMTIzNDU2Nzg5MA==";

    @BeforeEach
    void setUp() {
        jwtGenerator = new JWTGenerator();

        ReflectionTestUtils.setField(
                jwtGenerator,
                "jwtSecret",
                secret
        );

        ReflectionTestUtils.setField(
                jwtGenerator,
                "jwtExpiration",
                3600000L
        );
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        // given
        UUID userId = UUID.randomUUID();
        String role = "USER";

        // when
        String token = jwtGenerator.generateToken(userId, role);

        // then
        assertThat(jwtGenerator.validateToken(token)).isTrue();
        assertThat(jwtGenerator.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtGenerator.extractRole(token)).isEqualTo(role);
    }

    @Test
    void generateRefreshToken_shouldCreateValidToken() {
        // given
        UUID userId = UUID.randomUUID();
        String role = "USER";

        // when
        String token =
                jwtGenerator.generateRefreshToken(userId, role);

        // then
        assertThat(jwtGenerator.validateToken(token)).isTrue();
        assertThat(jwtGenerator.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtGenerator.extractRole(token)).isEqualTo(role);
    }

    @Test
    void generateServiceToken_shouldCreateValidServiceToken() {
        // given
        String serviceName = "station-service";
        String audience = "auth-service";
        String scope = "station.internal.station-details.read";

        // when
        String token =
                jwtGenerator.generateServiceToken(
                        serviceName,
                        audience,
                        scope
                );

        // then
        Claims claims = jwtGenerator.extractClaims(token);

        assertThat(jwtGenerator.validateToken(token)).isTrue();
        assertThat(claims.getIssuer()).isEqualTo("auth-service");
        assertThat(claims.getSubject()).isEqualTo(serviceName);
        assertThat(claims.getAudience()).isEqualTo(audience);
        assertThat(claims.get("role", String.class))
                .isEqualTo("SERVICE");
        assertThat(claims.get("scope", String.class))
                .isEqualTo(scope);
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenIsInvalid() {
        // given
        String token = "invalid.jwt.token";

        // when
        boolean result = jwtGenerator.validateToken(token);

        // then
        assertThat(result).isFalse();
    }
}
