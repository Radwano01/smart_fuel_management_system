package com.example.smart_fuel_management_system.security;

import com.example.smart_fuel_management_system.rateLimit.RateLimitRule;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @Mock
    private RedisRateLimiter rateLimiter;

    @Mock
    private FilterChain filterChain;

    private RateLimitingFilter filter() {
        return new RateLimitingFilter(rateLimiter);
    }

    private MockHttpServletRequest requestFor(String method, String path, String principalName) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(method);
        request.setRequestURI(path);
        if (principalName != null) {
            Principal principal = () -> principalName;
            request.setUserPrincipal(principal);
        }
        return request;
    }

    @Test
    void doFilterInternal_shouldPassThrough_whenNoRuleMatchesMethod() throws Exception {
        // given
        MockHttpServletRequest request = requestFor("POST", "/api/v1/users", "alice");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter().doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(rateLimiter);
    }

    @Test
    void doFilterInternal_shouldPassThrough_whenPathHasNoRule() throws Exception {
        // given
        MockHttpServletRequest request = requestFor("GET", "/api/v1/unrelated", "alice");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter().doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(rateLimiter);
    }

    @Test
    void doFilterInternal_shouldUseGetUserRule_whenGetOnUsersPath() throws Exception {
        // given
        MockHttpServletRequest request = requestFor("GET", "/api/v1/users", "alice");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        verify(rateLimiter).isAllowed(
                "PUBLIC:/api/v1/users:alice",
                RateLimitRule.GET_USER.capacity,
                RateLimitRule.GET_USER.duration.getSeconds());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldUseUpdateUserRule_whenPatchOnUsersPath() throws Exception {
        // given
        MockHttpServletRequest request = requestFor("PATCH", "/api/v1/users", "alice");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        verify(rateLimiter).isAllowed(
                "PUBLIC:/api/v1/users:alice",
                RateLimitRule.UPDATE_USER.capacity,
                RateLimitRule.UPDATE_USER.duration.getSeconds());
    }

    @Test
    void doFilterInternal_shouldUseInternalRule_whenGetOnUsersInternalPath() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("GET", "/api/v1/users/internal/by-ids", "svc");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        verify(rateLimiter).isAllowed(
                "INTERNAL:/api/v1/users/internal/by-ids:svc",
                RateLimitRule.INTERNAL_GET_USER.capacity,
                RateLimitRule.INTERNAL_GET_USER.duration.getSeconds());
    }

    @Test
    void doFilterInternal_shouldUsePrincipalName_whenPrincipalPresent() throws Exception {
        // given
        MockHttpServletRequest request = requestFor("GET", "/api/v1/users", "alice");
        request.setRemoteAddr("10.0.0.7");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        verify(rateLimiter).isAllowed(
                eq("PUBLIC:/api/v1/users:alice"), anyLong(), anyLong());
    }

    @Test
    void doFilterInternal_shouldUseRemoteAddr_whenPrincipalAbsent() throws Exception {
        // given
        MockHttpServletRequest request = requestFor("GET", "/api/v1/users", null);
        request.setRemoteAddr("10.0.0.7");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        verify(rateLimiter).isAllowed(
                eq("PUBLIC:/api/v1/users:10.0.0.7"), anyLong(), anyLong());
    }

    @Test
    void doFilterInternal_shouldReturn429AndBody_whenLimiterDenies() throws Exception {
        // given
        MockHttpServletRequest request = requestFor("GET", "/api/v1/users", "alice");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(false);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentAsString()).isEqualTo("Too many requests");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldContinueChain_whenLimiterAllows() throws Exception {
        // given
        MockHttpServletRequest request = requestFor("GET", "/api/v1/users", "alice");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void doFilterInternal_shouldNotRateLimit_whenPathIsActualInternalControllerPath() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("GET", "/api/v1/internal/users/by-ids", "svc");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter().doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(rateLimiter);
    }
}