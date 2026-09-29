package com.example.smart_fuel_management_system.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String BASE64_SECRET = Base64.getEncoder().encodeToString(
            "this-is-a-32-byte-secret-key-for-jwt!".getBytes(StandardCharsets.UTF_8));

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", BASE64_SECRET);
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(BASE64_SECRET));
    }

    @Test
    void generateServiceToken_setsExpectedClaims() {
        // given
        String serviceName = "dashboard-service";
        String audience = "transaction-service";
        String scope = "transaction.read";

        // when
        String token = jwtService.generateServiceToken(serviceName, audience, scope);

        // then
        Claims claims = jwtService.extractClaims(token);
        assertThat(claims.getIssuer()).isEqualTo("transaction-service");
        assertThat(claims.getSubject()).isEqualTo(serviceName);
        assertThat(claims.get("role", String.class)).isEqualTo("SERVICE");
        assertThat(claims.get("scope", String.class)).isEqualTo(scope);
        assertThat(claims.getAudience()).isEqualTo(audience);
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void generateServiceToken_producesValidToken() {
        // given
        String token = jwtService.generateServiceToken(
                "dashboard-service", "transaction-service", "transaction.read");

        // when
        boolean valid = jwtService.validateToken(token);

        // then
        assertThat(valid).isTrue();
    }

    @Test
    void extractRole_returnsRoleClaim() {
        // given
        String token = jwtService.generateServiceToken(
                "dashboard-service", "transaction-service", "transaction.read");

        // when
        String role = jwtService.extractRole(token);

        // then
        assertThat(role).isEqualTo("SERVICE");
    }

    @Test
    void extractIssuer_returnsIssuer() {
        // given
        String token = jwtService.generateServiceToken(
                "dashboard-service", "transaction-service", "transaction.read");

        // when
        String issuer = jwtService.extractIssuer(token);

        // then
        assertThat(issuer).isEqualTo("transaction-service");
    }

    @Test
    void extractAudience_returnsAudience() {
        // given
        String token = jwtService.generateServiceToken(
                "dashboard-service", "transaction-service", "transaction.read");

        // when
        String audience = jwtService.extractAudience(token);

        // then
        assertThat(audience).isEqualTo("transaction-service");
    }

    @Test
    void extractSubject_returnsSubject() {
        // given
        String token = jwtService.generateServiceToken(
                "station-service", "transaction-service", "transaction.read");

        // when
        String subject = jwtService.extractSubject(token);

        // then
        assertThat(subject).isEqualTo("station-service");
    }

    @Test
    void extractUserId_parsesSubjectAsUuid() {
        // given
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateServiceToken(
                userId.toString(), "transaction-service", "transaction.read");

        // when
        UUID extracted = jwtService.extractUserId(token);

        // then
        assertThat(extracted).isEqualTo(userId);
    }

    @Test
    void extractUserId_throwsWhenSubjectIsNotUuid() {
        // given
        String token = jwtService.generateServiceToken(
                "not-a-uuid", "transaction-service", "transaction.read");

        // when / then
        assertThatThrownBy(() -> jwtService.extractUserId(token))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateToken_returnsTrueForValidToken() {
        // given
        String token = jwtService.generateServiceToken(
                "dashboard-service", "transaction-service", "transaction.read");

        // when
        boolean valid = jwtService.validateToken(token);

        // then
        assertThat(valid).isTrue();
    }

    @Test
    void validateToken_returnsFalseForMalformedToken() {
        // given
        String malformed = "not-a-jwt";

        // when
        boolean valid = jwtService.validateToken(malformed);

        // then
        assertThat(valid).isFalse();
    }

    @Test
    void validateToken_returnsFalseForExpiredToken() {
        // given
        long now = System.currentTimeMillis();
        String expiredToken = Jwts.builder()
                .setIssuer("transaction-service")
                .setSubject("dashboard-service")
                .setAudience("transaction-service")
                .claim("role", "SERVICE")
                .setIssuedAt(new Date(now - 10_000))
                .setExpiration(new Date(now - 5_000))
                .signWith(key())
                .compact();

        // when
        boolean valid = jwtService.validateToken(expiredToken);

        // then
        assertThat(valid).isFalse();
    }

    @Test
    void validateToken_returnsFalseForTokenSignedWithDifferentKey() {
        // given
        SecretKey otherKey = Keys.hmacShaKeyFor(
                "another-32-byte-secret-key-for-jwt!!".getBytes(StandardCharsets.UTF_8));
        String foreignToken = Jwts.builder()
                .setIssuer("transaction-service")
                .setSubject("dashboard-service")
                .setAudience("transaction-service")
                .claim("role", "SERVICE")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(otherKey)
                .compact();

        // when
        boolean valid = jwtService.validateToken(foreignToken);

        // then
        assertThat(valid).isFalse();
    }
}