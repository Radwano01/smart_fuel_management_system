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
    void generateServiceToken_shouldSetExpectedClaims_whenCalled() {
        // given
        String serviceName = "auth-service";
        String audience = "user-service";
        String scope = "user.read";

        // when
        String token = jwtService.generateServiceToken(serviceName, audience, scope);

        // then
        Claims claims = jwtService.extractClaims(token);
        assertThat(claims.getIssuer()).isEqualTo("user-service");
        assertThat(claims.getSubject()).isEqualTo(serviceName);
        assertThat(claims.get("role", String.class)).isEqualTo("SERVICE");
        assertThat(claims.get("scope", String.class)).isEqualTo(scope);
        assertThat(claims.getAudience()).isEqualTo(audience);
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void generateServiceToken_shouldProduceValidToken_whenCalled() {
        // given
        String token = jwtService.generateServiceToken(
                "auth-service", "user-service", "user.read");

        // when
        boolean valid = jwtService.validateToken(token);

        // then
        assertThat(valid).isTrue();
    }

    @Test
    void extractRole_shouldReturnServiceRole_whenTokenGeneratedByService() {
        // given
        String token = jwtService.generateServiceToken(
                "auth-service", "user-service", "user.read");

        // when
        String role = jwtService.extractRole(token);

        // then
        assertThat(role).isEqualTo("SERVICE");
    }

    @Test
    void extractIssuer_shouldReturnUserService_whenTokenGeneratedLocally() {
        // given
        String token = jwtService.generateServiceToken(
                "auth-service", "user-service", "user.read");

        // when
        String issuer = jwtService.extractIssuer(token);

        // then
        assertThat(issuer).isEqualTo("user-service");
    }

    @Test
    void extractAudience_shouldReturnConfiguredAudience_whenCalled() {
        // given
        String token = jwtService.generateServiceToken(
                "auth-service", "user-service", "user.read");

        // when
        String audience = jwtService.extractAudience(token);

        // then
        assertThat(audience).isEqualTo("user-service");
    }

    @Test
    void extractSubject_shouldReturnServiceName_whenTokenGeneratedLocally() {
        // given
        String token = jwtService.generateServiceToken(
                "payment-service", "user-service", "user.read");

        // when
        String subject = jwtService.extractSubject(token);

        // then
        assertThat(subject).isEqualTo("payment-service");
    }

    @Test
    void extractUserId_shouldParseSubjectAsUuid_whenSubjectIsUuid() {
        // given
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateServiceToken(
                userId.toString(), "user-service", "user.read");

        // when
        UUID extracted = jwtService.extractUserId(token);

        // then
        assertThat(extracted).isEqualTo(userId);
    }

    @Test
    void extractUserId_shouldThrow_whenSubjectIsNotUuid() {
        // given
        String token = jwtService.generateServiceToken(
                "not-a-uuid", "user-service", "user.read");

        // when / then
        assertThatThrownBy(() -> jwtService.extractUserId(token))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateToken_shouldReturnTrue_whenTokenIsValid() {
        // given
        String token = jwtService.generateServiceToken(
                "auth-service", "user-service", "user.read");

        // when
        boolean valid = jwtService.validateToken(token);

        // then
        assertThat(valid).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenIsMalformed() {
        // given
        String malformed = "not-a-jwt";

        // when
        boolean valid = jwtService.validateToken(malformed);

        // then
        assertThat(valid).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenIsExpired() {
        // given
        long now = System.currentTimeMillis();
        String expiredToken = Jwts.builder()
                .setIssuer("user-service")
                .setSubject("auth-service")
                .setAudience("user-service")
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
    void validateToken_shouldReturnFalse_whenSignedWithDifferentKey() {
        // given
        SecretKey otherKey = Keys.hmacShaKeyFor(
                "another-32-byte-secret-key-for-jwt!!".getBytes(StandardCharsets.UTF_8));
        String foreignToken = Jwts.builder()
                .setIssuer("user-service")
                .setSubject("auth-service")
                .setAudience("user-service")
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