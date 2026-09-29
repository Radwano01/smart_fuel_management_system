package com.example.smart_fuel_management_system.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JWTServiceTest {

    private JWTService jwtService;

    private final String secret =
            "VGhpc0lzQVN1ZmZpY2llbnRseUxvbmdTZWNyZXRLZXlGb3JUZXN0aW5nMTIzNDU2Nzg5MA==";

    @BeforeEach
    void setUp() {
        jwtService = new JWTService();

        ReflectionTestUtils.setField(
                jwtService,
                "jwtSecret",
                secret
        );
    }

    @Test
    void generateServiceToken_shouldCreateValidServiceToken() {
        // given
        String serviceName = "dashboard-service";
        String audience = "auth-service";
        String scope = "dashboard.internal.read";

        // when
        String token =
                jwtService.generateServiceToken(
                        serviceName,
                        audience,
                        scope
                );

        // then
        assertThat(jwtService.validateToken(token))
                .isTrue();

        Claims claims =
                jwtService.extractClaims(token);

        assertThat(claims.getIssuer())
                .isEqualTo("dashboard-service");

        assertThat(claims.getSubject())
                .isEqualTo(serviceName);

        assertThat(claims.getAudience())
                .isEqualTo(audience);

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
        boolean result =
                jwtService.validateToken(token);

        // then
        assertThat(result)
                .isFalse();
    }

    @Test
    void extractUserId_shouldReturnUserId_whenTokenContainsValidUserId() {
        // given
        UUID userId = UUID.randomUUID();

        String token =
                createToken(
                        userId.toString(),
                        "USER"
                );

        // when
        UUID result =
                jwtService.extractUserId(token);

        // then
        assertThat(result)
                .isEqualTo(userId);
    }

    @Test
    void extractRole_shouldReturnRole_whenTokenContainsRole() {
        // given
        String token =
                createToken(
                        UUID.randomUUID().toString(),
                        "USER"
                );

        // when
        String result =
                jwtService.extractRole(token);

        // then
        assertThat(result)
                .isEqualTo("USER");
    }

    @Test
    void extractSubject_shouldReturnSubject_whenTokenContainsSubject() {
        // given
        String subject = UUID.randomUUID().toString();

        String token =
                createToken(
                        subject,
                        "USER"
                );

        // when
        String result =
                jwtService.extractSubject(token);

        // then
        assertThat(result)
                .isEqualTo(subject);
    }

    @Test
    void extractIssuer_shouldReturnIssuer_whenTokenContainsIssuer() {
        // given
        String token =
                createToken(
                        UUID.randomUUID().toString(),
                        "USER"
                );

        // when
        String result =
                jwtService.extractIssuer(token);

        // then
        assertThat(result)
                .isEqualTo("test-service");
    }

    @Test
    void extractAudience_shouldReturnAudience_whenTokenContainsAudience() {
        // given
        String token =
                createToken(
                        UUID.randomUUID().toString(),
                        "USER"
                );

        // when
        String result =
                jwtService.extractAudience(token);

        // then
        assertThat(result)
                .isEqualTo("test-audience");
    }

    private String createToken(String subject, String role) {
        return io.jsonwebtoken.Jwts.builder()
                .setIssuer("test-service")
                .setSubject(subject)
                .setAudience("test-audience")
                .claim("role", role)
                .setIssuedAt(new java.util.Date())
                .setExpiration(
                        new java.util.Date(
                                System.currentTimeMillis() + 3600000
                        )
                )
                .signWith(
                        Keys.hmacShaKeyFor(
                                Decoders.BASE64.decode(secret)
                        )
                )
                .compact();
    }
}
