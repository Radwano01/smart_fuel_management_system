package com.example.smart_fuel_management_system.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JWTAuthenticationFilterTest {

    @Mock
    private JWTGenerator jwtGenerator;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JWTAuthenticationFilter filter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldContinueChain_whenAuthorizationHeaderIsMissing() throws Exception {
        // given
        when(request.getHeader("Authorization")).thenReturn(null);

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtGenerator);
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();
    }

    @Test
    void doFilterInternal_shouldContinueChain_whenAuthorizationHeaderIsNotBearer() throws Exception {
        // given
        when(request.getHeader("Authorization"))
                .thenReturn("Basic some-token");

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtGenerator);
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();
    }

    @Test
    void doFilterInternal_shouldSetAuthentication_whenTokenIsValid() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        String token = "valid-token";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);
        when(jwtGenerator.validateToken(token))
                .thenReturn(true);
        when(jwtGenerator.extractRole(token))
                .thenReturn("USER");
        when(jwtGenerator.extractSubject(token))
                .thenReturn(userId.toString());

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo(userId.toString());
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactly("USER");

        verify(jwtGenerator).validateToken(token);
        verify(jwtGenerator).extractRole(token);
        verify(jwtGenerator).extractSubject(token);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldClearContext_whenTokenIsInvalid() throws Exception {
        // given
        String token = "invalid-token";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);
        when(jwtGenerator.validateToken(token))
                .thenReturn(false);

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(jwtGenerator).validateToken(token);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldClearContext_whenTokenProcessingThrowsException() throws Exception {
        // given
        String token = "invalid-token";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);
        when(jwtGenerator.validateToken(token))
                .thenThrow(new RuntimeException("JWT error"));

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(jwtGenerator).validateToken(token);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldSetAuthentication_whenServiceTokenIsValid() throws Exception {
        // given
        String token = "service-token";
        UUID serviceId = UUID.randomUUID();

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);
        when(jwtGenerator.validateToken(token))
                .thenReturn(true);
        when(jwtGenerator.extractRole(token))
                .thenReturn("SERVICE");
        when(jwtGenerator.extractIssuer(token))
                .thenReturn("station-service");
        when(jwtGenerator.extractAudience(token))
                .thenReturn("auth-service");
        when(jwtGenerator.extractSubject(token))
                .thenReturn(serviceId.toString());

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo(serviceId.toString());
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactly("SERVICE");

        verify(jwtGenerator).extractIssuer(token);
        verify(jwtGenerator).extractAudience(token);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldClearContext_whenServiceIssuerIsInvalid() throws Exception {
        // given
        String token = "service-token";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);
        when(jwtGenerator.validateToken(token))
                .thenReturn(true);
        when(jwtGenerator.extractRole(token))
                .thenReturn("SERVICE");
        when(jwtGenerator.extractIssuer(token))
                .thenReturn("payment-service");
        when(jwtGenerator.extractAudience(token))
                .thenReturn("auth-service");

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(jwtGenerator).extractIssuer(token);
        verify(jwtGenerator).extractAudience(token);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldClearContext_whenServiceAudienceIsInvalid() throws Exception {
        // given
        String token = "service-token";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);
        when(jwtGenerator.validateToken(token))
                .thenReturn(true);
        when(jwtGenerator.extractRole(token))
                .thenReturn("SERVICE");
        when(jwtGenerator.extractIssuer(token))
                .thenReturn("station-service");
        when(jwtGenerator.extractAudience(token))
                .thenReturn("payment-service");

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(jwtGenerator).extractIssuer(token);
        verify(jwtGenerator).extractAudience(token);
        verify(filterChain).doFilter(request, response);
    }
}
