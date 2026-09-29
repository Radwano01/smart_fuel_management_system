package com.example.smart_fuel_management_system.security;

import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @Mock
    private RedisRateLimiter rateLimiter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter writer;

    @InjectMocks
    private RateLimitingFilter filter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldContinueChain_whenPathHasNoRateLimitRule()
            throws Exception {
        // given
        when(request.getRequestURI())
                .thenReturn("/api/v1/users");

        // when
        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        verify(filterChain)
                .doFilter(request, response);

        verifyNoInteractions(rateLimiter);
    }

    @Test
    void doFilterInternal_shouldContinueChain_whenRequestIsAllowed()
            throws Exception {
        // given
        String path = "/api/v1/auth/login";
        String ip = "192.168.1.10";

        when(request.getRequestURI())
                .thenReturn(path);

        when(request.getUserPrincipal())
                .thenReturn(null);

        when(request.getRemoteAddr())
                .thenReturn(ip);

        when(rateLimiter.isAllowed(
                eq(path + ":" + ip),
                anyLong(),
                anyLong()
        )).thenReturn(true);

        // when
        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        verify(rateLimiter)
                .isAllowed(
                        eq(path + ":" + ip),
                        anyLong(),
                        anyLong()
                );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldReturnTooManyRequests_whenLimitIsExceeded()
            throws Exception {
        // given
        String path = "/api/v1/auth/login";
        String ip = "192.168.1.10";

        when(request.getRequestURI())
                .thenReturn(path);

        when(request.getUserPrincipal())
                .thenReturn(null);

        when(request.getRemoteAddr())
                .thenReturn(ip);

        when(rateLimiter.isAllowed(
                eq(path + ":" + ip),
                anyLong(),
                anyLong()
        )).thenReturn(false);

        when(response.getWriter())
                .thenReturn(writer);

        // when
        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        verify(response)
                .setStatus(429);

        verify(response.getWriter())
                .write("Too many requests");

        verifyNoInteractions(filterChain);
    }

    @Test
    void doFilterInternal_shouldUseAuthenticatedUser_whenPrincipalExists()
            throws Exception {
        // given
        String path = "/api/v1/auth/login";
        String username = "user-id";

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null
                );

        when(request.getRequestURI())
                .thenReturn(path);

        when(request.getUserPrincipal())
                .thenReturn(authentication);

        when(rateLimiter.isAllowed(
                eq(path + ":" + username),
                anyLong(),
                anyLong()
        )).thenReturn(true);

        // when
        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        verify(rateLimiter)
                .isAllowed(
                        eq(path + ":" + username),
                        anyLong(),
                        anyLong()
                );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldUseLocalhost_whenRemoteAddressIsIpv6Localhost()
            throws Exception {
        // given
        String path = "/api/v1/auth/login";

        when(request.getRequestURI())
                .thenReturn(path);

        when(request.getUserPrincipal())
                .thenReturn(null);

        when(request.getRemoteAddr())
                .thenReturn("::1");

        when(rateLimiter.isAllowed(
                eq(path + ":localhost"),
                anyLong(),
                anyLong()
        )).thenReturn(true);

        // when
        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        verify(rateLimiter)
                .isAllowed(
                        eq(path + ":localhost"),
                        anyLong(),
                        anyLong()
                );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldUseLocalhost_whenRemoteAddressIsFullIpv6Localhost()
            throws Exception {
        // given
        String path = "/api/v1/auth/login";

        when(request.getRequestURI())
                .thenReturn(path);

        when(request.getUserPrincipal())
                .thenReturn(null);

        when(request.getRemoteAddr())
                .thenReturn("0:0:0:0:0:0:0:1");

        when(rateLimiter.isAllowed(
                eq(path + ":localhost"),
                anyLong(),
                anyLong()
        )).thenReturn(true);

        // when
        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        verify(rateLimiter)
                .isAllowed(
                        eq(path + ":localhost"),
                        anyLong(),
                        anyLong()
                );

        verify(filterChain)
                .doFilter(request, response);
    }
}
