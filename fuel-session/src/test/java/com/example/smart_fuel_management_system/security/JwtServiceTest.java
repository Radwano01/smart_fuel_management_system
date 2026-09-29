package com.example.smart_fuel_management_system.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private String secret;

    @BeforeEach
    void setUp() {
        secret = Base64.getEncoder().encodeToString(
                "this-is-a-test-secret-key-with-at-least-32-bytes".getBytes()
        );

        ReflectionTestUtils.setField(
                jwtService,
                "jwtSecret",
                secret
        );
    }

    @Test
    void generateServiceToken_shouldCreateValidToken_withExpectedClaims() {
        // given
        String serviceName = "fuel-session-service";
        String audience = "station-service";
        String scope = "station.internal.price.read";

        // when
        String token = jwtService.generateServiceToken(
                serviceName,
                audience,
                scope
        );

        // then
        Claims claims = jwtService.extractClaims(token);

        assertThat(claims.getIssuer())
                .isEqualTo("fuel-session-service");

        assertThat(claims.getSubject())
                .isEqualTo(serviceName);

        assertThat(claims.getAudience())
                .isEqualTo(audience);

        assertThat(claims.get("role", String.class))
                .isEqualTo("SERVICE");

        assertThat(claims.get("scope", String.class))
                .isEqualTo(scope);

        assertThat(claims.getIssuedAt())
                .isNotNull();

        assertThat(claims.getExpiration())
                .isNotNull();

        assertThat(claims.getExpiration())
                .isAfter(claims.getIssuedAt());
    }

    @Test
    void extractClaims_shouldReturnClaims_whenTokenIsValid() {
        // given
        String token = jwtService.generateServiceToken(
                "payment-service",
                "payment-service",
                "payment.internal.preauth"
        );

        // when
        Claims claims = jwtService.extractClaims(token);

        // then
        assertThat(claims)
                .isNotNull();

        assertThat(claims.getSubject())
                .isEqualTo("payment-service");
    }

    @Test
    void extractUserId_shouldReturnUserId_whenSubjectIsValidUuid() {
        // given
        UUID userId = UUID.randomUUID();

        String token = Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + 300000)
                )
                .signWith(
                        (javax.crypto.SecretKey) ReflectionTestUtils.invokeMethod(
                                jwtService,
                                "key"
                        )
                )
                .compact();

        // when
        UUID result = jwtService.extractUserId(token);

        // then
        assertThat(result)
                .isEqualTo(userId);
    }

    @Test
    void extractBearerToken_shouldReturnToken_whenAuthorizationHeaderIsBearer() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(
                "Authorization",
                "Bearer test-token"
        );

        // when
        String result = jwtService.extractBearerToken(request);

        // then
        assertThat(result)
                .isEqualTo("test-token");
    }

    @Test
    void extractBearerToken_shouldReturnNull_whenAuthorizationHeaderIsMissing() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();

        // when
        String result = jwtService.extractBearerToken(request);

        // then
        assertThat(result)
                .isNull();
    }

    @Test
    void extractBearerToken_shouldReturnNull_whenAuthorizationHeaderIsNotBearer() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(
                "Authorization",
                "Basic test-token"
        );

        // when
        String result = jwtService.extractBearerToken(request);

        // then
        assertThat(result)
                .isNull();
    }

    @Test
    void extractRole_shouldReturnRole_whenTokenIsValid() {
        // given
        String token = jwtService.generateServiceToken(
                "fuel-session-service",
                "station-service",
                "station.internal.price.read"
        );

        // when
        String result = jwtService.extractRole(token);

        // then
        assertThat(result)
                .isEqualTo("SERVICE");
    }

    @Test
    void extractIssuer_shouldReturnIssuer_whenTokenIsValid() {
        // given
        String token = jwtService.generateServiceToken(
                "fuel-session-service",
                "station-service",
                "station.internal.price.read"
        );

        // when
        String result = jwtService.extractIssuer(token);

        // then
        assertThat(result)
                .isEqualTo("fuel-session-service");
    }

    @Test
    void extractAudience_shouldReturnAudience_whenTokenIsValid() {
        // given
        String token = jwtService.generateServiceToken(
                "fuel-session-service",
                "station-service",
                "station.internal.price.read"
        );

        // when
        String result = jwtService.extractAudience(token);

        // then
        assertThat(result)
                .isEqualTo("station-service");
    }

    @Test
    void validateToken_shouldReturnTrue_whenTokenIsValid() {
        // given
        String token = jwtService.generateServiceToken(
                "fuel-session-service",
                "station-service",
                "station.internal.price.read"
        );

        // when
        boolean result = jwtService.validateToken(token);

        // then
        assertThat(result)
                .isTrue();
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenIsInvalid() {
        // given
        String token = "invalid-token";

        // when
        boolean result = jwtService.validateToken(token);

        // then
        assertThat(result)
                .isFalse();
    }

    @Test
    void extractSubject_shouldReturnSubject_whenTokenIsValid() {
        // given
        String token = jwtService.generateServiceToken(
                "fuel-session-service",
                "station-service",
                "station.internal.price.read"
        );

        // when
        String result = jwtService.extractSubject(token);

        // then
        assertThat(result)
                .isEqualTo("fuel-session-service");
    }
}