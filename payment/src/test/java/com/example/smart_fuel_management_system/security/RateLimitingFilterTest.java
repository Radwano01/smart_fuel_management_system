package com.example.smart_fuel_management_system.security;

import com.example.smart_fuel_management_system.rateLimit.RateLimitRule;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.security.Principal;

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
    private Principal principal;

    private RateLimitingFilter rateLimitingFilter;

    @BeforeEach
    void setUp() {
        rateLimitingFilter = new RateLimitingFilter(rateLimiter);
    }

    @Test
    void doFilterInternal_shouldContinueRequest_whenNoRateLimitRuleExists()
            throws Exception {
        // given
        when(request.getRequestURI()).thenReturn("/api/v1/unknown");
        when(request.getMethod()).thenReturn("GET");

        // when
        rateLimitingFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(rateLimiter);
    }

    @Test
    void doFilterInternal_shouldContinueRequest_whenRequestIsAllowed()
            throws Exception {
        // given
        String path = "/api/v1/payments";

        when(request.getRequestURI()).thenReturn(path);
        when(request.getMethod()).thenReturn("GET");
        when(request.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("user-123");

        when(rateLimiter.isAllowed(
                "PUBLIC:" + path + ":user-123",
                RateLimitRule.GET_PAYMENT.capacity,
                RateLimitRule.GET_PAYMENT.duration.getSeconds()
        )).thenReturn(true);

        // when
        rateLimitingFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        verify(rateLimiter).isAllowed(
                "PUBLIC:" + path + ":user-123",
                RateLimitRule.GET_PAYMENT.capacity,
                RateLimitRule.GET_PAYMENT.duration.getSeconds()
        );

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    void doFilterInternal_shouldReturnTooManyRequests_whenRateLimitExceeded()
            throws Exception {
        // given
        String path = "/api/v1/payments";

        when(request.getRequestURI()).thenReturn(path);
        when(request.getMethod()).thenReturn("POST");
        when(request.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("user-123");

        when(rateLimiter.isAllowed(
                "PUBLIC:" + path + ":user-123",
                RateLimitRule.CREATE_PAYMENT.capacity,
                RateLimitRule.CREATE_PAYMENT.duration.getSeconds()
        )).thenReturn(false);

        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // when
        rateLimitingFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        verify(rateLimiter).isAllowed(
                "PUBLIC:" + path + ":user-123",
                RateLimitRule.CREATE_PAYMENT.capacity,
                RateLimitRule.CREATE_PAYMENT.duration.getSeconds()
        );

        verify(response).setStatus(429);
        verify(response.getWriter()).write("Too many requests");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldUseRemoteAddress_whenUserIsNotAuthenticated()
            throws Exception {
        // given
        String path = "/api/v1/payments";

        when(request.getRequestURI()).thenReturn(path);
        when(request.getMethod()).thenReturn("GET");
        when(request.getUserPrincipal()).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.10");

        when(rateLimiter.isAllowed(
                "PUBLIC:" + path + ":192.168.1.10",
                RateLimitRule.GET_PAYMENT.capacity,
                RateLimitRule.GET_PAYMENT.duration.getSeconds()
        )).thenReturn(true);

        // when
        rateLimitingFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        verify(rateLimiter).isAllowed(
                "PUBLIC:" + path + ":192.168.1.10",
                RateLimitRule.GET_PAYMENT.capacity,
                RateLimitRule.GET_PAYMENT.duration.getSeconds()
        );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldApplySuccessWebhookRule_whenSuccessWebhookIsRequested()
            throws Exception {
        // given
        String path = "/api/v1/payments/webhook/success";

        when(request.getRequestURI()).thenReturn(path);
        when(request.getMethod()).thenReturn("POST");
        when(request.getUserPrincipal()).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.10");

        when(rateLimiter.isAllowed(
                "PUBLIC:" + path + ":192.168.1.10",
                RateLimitRule.PAYMENT_SUCCESS_WEBHOOK.capacity,
                RateLimitRule.PAYMENT_SUCCESS_WEBHOOK.duration.getSeconds()
        )).thenReturn(true);

        // when
        rateLimitingFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        verify(rateLimiter).isAllowed(
                "PUBLIC:" + path + ":192.168.1.10",
                RateLimitRule.PAYMENT_SUCCESS_WEBHOOK.capacity,
                RateLimitRule.PAYMENT_SUCCESS_WEBHOOK.duration.getSeconds()
        );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldApplyFailedWebhookRule_whenFailedWebhookIsRequested()
            throws Exception {
        // given
        String path = "/api/v1/payments/webhook/failed";

        when(request.getRequestURI()).thenReturn(path);
        when(request.getMethod()).thenReturn("POST");
        when(request.getUserPrincipal()).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.10");

        when(rateLimiter.isAllowed(
                "PUBLIC:" + path + ":192.168.1.10",
                RateLimitRule.PAYMENT_FAILED_WEBHOOK.capacity,
                RateLimitRule.PAYMENT_FAILED_WEBHOOK.duration.getSeconds()
        )).thenReturn(true);

        // when
        rateLimitingFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        // then
        verify(rateLimiter).isAllowed(
                "PUBLIC:" + path + ":192.168.1.10",
                RateLimitRule.PAYMENT_FAILED_WEBHOOK.capacity,
                RateLimitRule.PAYMENT_FAILED_WEBHOOK.duration.getSeconds()
        );

        verify(filterChain).doFilter(request, response);
    }
}
