package com.example.smart_fuel_management_system.security;

import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;

import static org.mockito.Mockito.*;

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

    @Test
    void doFilterInternal_shouldContinueChain_whenPathHasNoRateLimitRule()
            throws Exception {
        // given
        when(request.getRequestURI()).thenReturn("/api/v1/users");

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(rateLimiter);
    }

    @Test
    void doFilterInternal_shouldContinueChain_whenRequestIsAllowed()
            throws Exception {
        // given
        String path = "/api/v1/auth/login";
        String ip = "192.168.1.10";

        when(request.getRequestURI()).thenReturn(path);
        when(request.getUserPrincipal()).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(ip);

        when(rateLimiter.isAllowed(
                eq(path + ":" + ip),
                anyLong(),
                anyLong()
        )).thenReturn(true);

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(rateLimiter).isAllowed(
                eq(path + ":" + ip),
                anyLong(),
                anyLong()
        );
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(response);
    }

    @Test
    void doFilterInternal_shouldReturnTooManyRequests_whenLimitIsExceeded()
            throws Exception {
        // given
        String path = "/api/v1/auth/login";
        String ip = "192.168.1.10";

        when(request.getRequestURI()).thenReturn(path);
        when(request.getUserPrincipal()).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(ip);
        when(rateLimiter.isAllowed(
                eq(path + ":" + ip),
                anyLong(),
                anyLong()
        )).thenReturn(false);
        when(response.getWriter()).thenReturn(writer);

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(response).setStatus(429);
        verify(response.getWriter()).write("Too many requests");
        verifyNoInteractions(filterChain);
    }

    @Test
    void doFilterInternal_shouldUseAuthenticatedUser_whenPrincipalExists()
            throws Exception {
        // given
        String path = "/api/v1/auth/login";
        String username = "user-id";

        java.security.Principal principal =
                mock(java.security.Principal.class);

        when(request.getRequestURI()).thenReturn(path);
        when(request.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn(username);

        when(rateLimiter.isAllowed(
                eq(path + ":" + username),
                anyLong(),
                anyLong()
        )).thenReturn(true);

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(rateLimiter).isAllowed(
                eq(path + ":" + username),
                anyLong(),
                anyLong()
        );
        verify(filterChain).doFilter(request, response);
        verify(request, never()).getRemoteAddr();
    }

    @Test
    void doFilterInternal_shouldUseLocalhost_whenRemoteAddressIsIpv6Localhost()
            throws Exception {
        // given
        String path = "/api/v1/auth/login";

        when(request.getRequestURI()).thenReturn(path);
        when(request.getUserPrincipal()).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("::1");

        when(rateLimiter.isAllowed(
                eq(path + ":localhost"),
                anyLong(),
                anyLong()
        )).thenReturn(true);

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(rateLimiter).isAllowed(
                eq(path + ":localhost"),
                anyLong(),
                anyLong()
        );
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldUseLocalhost_whenRemoteAddressIsFullIpv6Localhost()
            throws Exception {
        // given
        String path = "/api/v1/auth/login";

        when(request.getRequestURI()).thenReturn(path);
        when(request.getUserPrincipal()).thenReturn(null);
        when(request.getRemoteAddr())
                .thenReturn("0:0:0:0:0:0:0:1");

        when(rateLimiter.isAllowed(
                eq(path + ":localhost"),
                anyLong(),
                anyLong()
        )).thenReturn(true);

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(rateLimiter).isAllowed(
                eq(path + ":localhost"),
                anyLong(),
                anyLong()
        );
        verify(filterChain).doFilter(request, response);
    }
}
