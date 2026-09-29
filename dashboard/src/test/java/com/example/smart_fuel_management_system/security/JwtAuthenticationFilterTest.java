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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JWTService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldContinueChain_whenAuthorizationHeaderIsMissing()
            throws Exception {
        // given
        when(request.getHeader("Authorization"))
                .thenReturn(null);

        // when
        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        verify(filterChain)
                .doFilter(request, response);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();
    }

    @Test
    void doFilterInternal_shouldContinueChain_whenAuthorizationHeaderIsNotBearer()
            throws Exception {
        // given
        when(request.getHeader("Authorization"))
                .thenReturn("Basic token");

        // when
        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        verify(filterChain)
                .doFilter(request, response);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();
    }

    @Test
    void doFilterInternal_shouldAuthenticateAdmin_whenTokenTypeIsAdmin()
            throws Exception {
        // given
        String token = "admin-token";
        String subject = "admin-id";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtService.validateToken(token))
                .thenReturn(true);

        when(jwtService.extractRole(token))
                .thenReturn("ADMIN");

        when(jwtService.extractSubject(token))
                .thenReturn(subject);

        // when
        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        assertThat(authentication)
                .isNotNull();

        assertThat(authentication.getPrincipal())
                .isEqualTo(subject);

        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ADMIN");

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldAuthenticateService_whenTokenTypeIsService()
            throws Exception {
        // given
        String token = "service-token";
        String subject = "station-service";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtService.validateToken(token))
                .thenReturn(true);

        when(jwtService.extractRole(token))
                .thenReturn("SERVICE");

        when(jwtService.extractSubject(token))
                .thenReturn(subject);

        // when
        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        assertThat(authentication)
                .isNotNull();

        assertThat(authentication.getPrincipal())
                .isEqualTo(subject);

        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("SERVICE");

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldAuthenticateUser_whenTokenTypeIsUser()
            throws Exception {
        // given
        String token = "user-token";
        UUID userId = UUID.randomUUID();

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtService.validateToken(token))
                .thenReturn(true);

        when(jwtService.extractRole(token))
                .thenReturn("USER");

        when(jwtService.extractUserId(token))
                .thenReturn(userId);

        // when
        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        assertThat(authentication)
                .isNotNull();

        assertThat(authentication.getPrincipal())
                .isEqualTo(userId);

        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("USER");

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldClearContext_whenTokenIsInvalid()
            throws Exception {
        // given
        String token = "invalid-token";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtService.validateToken(token))
                .thenReturn(false);

        // when
        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldClearContext_whenTokenTypeIsUnknown()
            throws Exception {
        // given
        String token = "unknown-token";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtService.validateToken(token))
                .thenReturn(true);

        when(jwtService.extractRole(token))
                .thenReturn("UNKNOWN");

        // when
        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(filterChain)
                .doFilter(request, response);
    }
}