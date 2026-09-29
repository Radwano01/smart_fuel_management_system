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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JWTAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JWTAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldContinueWithoutAuthentication_whenAuthorizationHeaderIsMissing()
            throws Exception {
        // given
        when(request.getHeader("Authorization"))
                .thenReturn(null);

        // when
        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldContinueWithoutAuthentication_whenAuthorizationHeaderIsNotBearer()
            throws Exception {
        // given
        when(request.getHeader("Authorization"))
                .thenReturn("Basic abc123");

        // when
        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldAuthenticateUser_whenTokenIsValid()
            throws Exception {
        // given
        String token = "valid-token";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtService.validateToken(token))
                .thenReturn(true);

        when(jwtService.extractRole(token))
                .thenReturn("USER");

        when(jwtService.extractSubject(token))
                .thenReturn("user-123");

        // when
        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal())
                .isEqualTo("user-123");
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("USER");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldAuthenticateService_whenServiceTokenHasValidIssuerAndAudience()
            throws Exception {
        // given
        String token = "service-token";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtService.validateToken(token))
                .thenReturn(true);

        when(jwtService.extractRole(token))
                .thenReturn("SERVICE");

        when(jwtService.extractIssuer(token))
                .thenReturn("fuel-session-service");

        when(jwtService.extractAudience(token))
                .thenReturn("payment-service");

        when(jwtService.extractSubject(token))
                .thenReturn("fuel-session-service");

        // when
        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal())
                .isEqualTo("fuel-session-service");
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("SERVICE");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenTokenIsInvalid()
            throws Exception {
        // given
        String token = "invalid-token";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtService.validateToken(token))
                .thenReturn(false);

        // when
        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenServiceTokenHasInvalidIssuer()
            throws Exception {
        // given
        String token = "service-token";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtService.validateToken(token))
                .thenReturn(true);

        when(jwtService.extractRole(token))
                .thenReturn("SERVICE");

        when(jwtService.extractIssuer(token))
                .thenReturn("wrong-service");

        // when
        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(filterChain).doFilter(request, response);
    }
}
