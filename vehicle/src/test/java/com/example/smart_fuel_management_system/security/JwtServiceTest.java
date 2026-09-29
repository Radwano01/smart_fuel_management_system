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

    private String buildToken(String subject, String role, String issuer, String audience) {
        Date now = new Date();
        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(subject)
                .setAudience(audience)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + 60_000))
                .signWith(key())
                .compact();
    }

    // ---------------------------------------------------------------------
    // extractClaims
    // ---------------------------------------------------------------------

    @Test
    void extractClaims_shouldReturnClaims_whenTokenIsValid() {
        // given
        String token = buildToken("subject-1", "USER", "user-service", "vehicle-service");

        // when
        Claims claims = jwtService.extractClaims(token);

        // then
        assertThat(claims.getSubject()).isEqualTo("subject-1");
        assertThat(claims.getIssuer()).isEqualTo("user-service");
        assertThat(claims.getAudience()).isEqualTo("vehicle-service");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
    }

    @Test
    void extractClaims_shouldThrow_whenTokenIsMalformed() {
        // given
        String malformed = "not-a-jwt";

        // when / then
        assertThatThrownBy(() -> jwtService.extractClaims(malformed))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractClaims_shouldThrow_whenTokenSignedWithDifferentKey() {
        // given
        SecretKey otherKey = Keys.hmacShaKeyFor(
                "another-32-byte-secret-key-for-jwt!!".getBytes(StandardCharsets.UTF_8));
        String foreignToken = Jwts.builder()
                .setIssuer("user-service")
                .setSubject("subject-1")
                .claim("role", "USER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(otherKey)
                .compact();

        // when / then
        assertThatThrownBy(() -> jwtService.extractClaims(foreignToken))
                .isInstanceOf(Exception.class);
    }

    // ---------------------------------------------------------------------
    // extractRole
    // ---------------------------------------------------------------------

    @Test
    void extractRole_shouldReturnRole_whenClaimPresent() {
        // given
        String token = buildToken("subject-1", "ADMIN", "user-service", "vehicle-service");

        // when
        String role = jwtService.extractRole(token);

        // then
        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    void extractRole_shouldReturnNull_whenClaimMissing() {
        // given
        String token = Jwts.builder()
                .setIssuer("user-service")
                .setSubject("subject-1")
                .setAudience("vehicle-service")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key())
                .compact();

        // when
        String role = jwtService.extractRole(token);

        // then
        assertThat(role).isNull();
    }

    // ---------------------------------------------------------------------
    // extractIssuer
    // ---------------------------------------------------------------------

    @Test
    void extractIssuer_shouldReturnIssuer_whenPresent() {
        // given
        String token = buildToken("subject-1", "USER", "dashboard-service", "vehicle-service");

        // when
        String issuer = jwtService.extractIssuer(token);

        // then
        assertThat(issuer).isEqualTo("dashboard-service");
    }

    // ---------------------------------------------------------------------
    // extractAudience
    // ---------------------------------------------------------------------

    @Test
    void extractAudience_shouldReturnAudience_whenPresent() {
        // given
        String token = buildToken("subject-1", "USER", "user-service", "vehicle-service");

        // when
        String audience = jwtService.extractAudience(token);

        // then
        assertThat(audience).isEqualTo("vehicle-service");
    }

    // ---------------------------------------------------------------------
    // extractSubject
    // ---------------------------------------------------------------------

    @Test
    void extractSubject_shouldReturnSubject_whenPresent() {
        // given
        String token = buildToken("dashboard-service", "SERVICE", "dashboard-service", "vehicle-service");

        // when
        String subject = jwtService.extractSubject(token);

        // then
        assertThat(subject).isEqualTo("dashboard-service");
    }

    // ---------------------------------------------------------------------
    // extractUserId
    // ---------------------------------------------------------------------

    @Test
    void extractUserId_shouldParseSubjectAsUuid_whenSubjectIsUuid() {
        // given
        UUID userId = UUID.randomUUID();
        String token = buildToken(userId.toString(), "USER", "user-service", "vehicle-service");

        // when
        UUID extracted = jwtService.extractUserId(token);

        // then
        assertThat(extracted).isEqualTo(userId);
    }

    @Test
    void extractUserId_shouldThrow_whenSubjectIsNotUuid() {
        // given
        String token = buildToken("not-a-uuid", "USER", "user-service", "vehicle-service");

        // when / then
        assertThatThrownBy(() -> jwtService.extractUserId(token))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---------------------------------------------------------------------
    // validateToken
    // ---------------------------------------------------------------------

    @Test
    void validateToken_shouldReturnTrue_whenTokenIsValid() {
        // given
        String token = buildToken("subject-1", "USER", "user-service", "vehicle-service");

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
                .setSubject("subject-1")
                .setAudience("vehicle-service")
                .claim("role", "USER")
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
                .setSubject("subject-1")
                .setAudience("vehicle-service")
                .claim("role", "USER")
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