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

    @Test
    void generateServiceToken_shouldSetExpectedClaims_whenCalled() {
        // given
        String serviceName = "station-service";
        String audience = "auth-service";
        String scope = "auth.internal.station-employee.get";

        // when
        String token = jwtService.generateServiceToken(serviceName, audience, scope);

        // then
        Claims claims = jwtService.extractClaims(token);
        assertThat(claims.getIssuer()).isEqualTo("station-service");
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
                "station-service", "auth-service", "auth.internal.station-employee.get");

        // when
        boolean valid = jwtService.validateToken(token);

        // then
        assertThat(valid).isTrue();
    }

    @Test
    void extractClaims_shouldReturnClaims_whenTokenIsValid() {
        // given
        String token = buildToken("subject-1", "STATION", "station-service", "auth-service");

        // when
        Claims claims = jwtService.extractClaims(token);

        // then
        assertThat(claims.getSubject()).isEqualTo("subject-1");
        assertThat(claims.getIssuer()).isEqualTo("station-service");
        assertThat(claims.getAudience()).isEqualTo("auth-service");
        assertThat(claims.get("role", String.class)).isEqualTo("STATION");
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
                .setIssuer("station-service")
                .setSubject("subject-1")
                .claim("role", "STATION")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(otherKey)
                .compact();

        // when / then
        assertThatThrownBy(() -> jwtService.extractClaims(foreignToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractRole_shouldReturnRole_whenClaimPresent() {
        // given
        String token = buildToken("subject-1", "ADMIN", "station-service", "auth-service");

        // when
        String role = jwtService.extractRole(token);

        // then
        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    void extractRole_shouldReturnNull_whenClaimMissing() {
        // given
        String token = Jwts.builder()
                .setIssuer("station-service")
                .setSubject("subject-1")
                .setAudience("auth-service")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key())
                .compact();

        // when
        String role = jwtService.extractRole(token);

        // then
        assertThat(role).isNull();
    }

    @Test
    void extractIssuer_shouldReturnIssuer_whenPresent() {
        // given
        String token = buildToken("subject-1", "STATION", "auth-service", "station-service");

        // when
        String issuer = jwtService.extractIssuer(token);

        // then
        assertThat(issuer).isEqualTo("auth-service");
    }

    @Test
    void extractAudience_shouldReturnAudience_whenPresent() {
        // given
        String token = buildToken("subject-1", "STATION", "station-service", "auth-service");

        // when
        String audience = jwtService.extractAudience(token);

        // then
        assertThat(audience).isEqualTo("auth-service");
    }

    @Test
    void extractSubject_shouldReturnSubject_whenPresent() {
        // given
        String token = buildToken(
                "auth-service", "SERVICE", "auth-service", "station-service");

        // when
        String subject = jwtService.extractSubject(token);

        // then
        assertThat(subject).isEqualTo("auth-service");
    }

    @Test
    void extractEmployeeId_shouldParseSubjectAsUuid_whenSubjectIsUuid() {
        // given
        UUID employeeId = UUID.randomUUID();
        String token = buildToken(
                employeeId.toString(), "STATION", "station-service", "auth-service");

        // when
        UUID extracted = jwtService.extractEmployeeId(token);

        // then
        assertThat(extracted).isEqualTo(employeeId);
    }

    @Test
    void extractEmployeeId_shouldThrow_whenSubjectIsNotUuid() {
        // given
        String token = buildToken(
                "not-a-uuid", "STATION", "station-service", "auth-service");

        // when / then
        assertThatThrownBy(() -> jwtService.extractEmployeeId(token))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateToken_shouldReturnTrue_whenTokenIsValid() {
        // given
        String token = buildToken(
                "subject-1", "STATION", "station-service", "auth-service");

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
                .setIssuer("station-service")
                .setSubject("subject-1")
                .setAudience("auth-service")
                .claim("role", "STATION")
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
                .setIssuer("station-service")
                .setSubject("subject-1")
                .setAudience("auth-service")
                .claim("role", "STATION")
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