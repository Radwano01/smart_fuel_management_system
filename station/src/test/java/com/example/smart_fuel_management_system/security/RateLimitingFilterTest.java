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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
        MockHttpServletRequest request =
                requestFor("GET", "/api/v1/admin/stations", "alice");
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
        MockHttpServletRequest request =
                requestFor("GET", "/api/v1/unrelated", "alice");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter().doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(rateLimiter);
    }

    @Test
    void doFilterInternal_shouldUseCreateStationRule_whenPostOnAdminStations() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("POST", "/api/v1/admin/stations", "alice");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        assertThat(RateLimitRule.CREATE_STATION.capacity).isEqualTo(5);
        verify(rateLimiter).isAllowed(
                "/api/v1/admin/stations:alice", 5, 600);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldUseChangeStationStatusRule_whenPatchOnStatus() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("PATCH", "/api/v1/admin/stations/abc/status", "alice");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        assertThat(RateLimitRule.CHANGE_STATION_STATUS.capacity).isEqualTo(10);
        verify(rateLimiter).isAllowed(
                "/api/v1/admin/stations/abc/status:alice", 10, 60);
    }

    @Test
    void doFilterInternal_shouldUseCreateFuelPriceRule_whenPostOnFuelPrices() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("POST", "/api/v1/admin/stations/abc/fuel-prices/GASOLINE", "alice");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        assertThat(RateLimitRule.CREATE_FUEL_PRICE.capacity).isEqualTo(20);
        verify(rateLimiter).isAllowed(
                "/api/v1/admin/stations/abc/fuel-prices/GASOLINE:alice", 20, 60);
    }

    @Test
    void doFilterInternal_shouldUseUpdateFuelPriceRule_whenPatchOnFuelPrices() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("PATCH", "/api/v1/admin/stations/abc/fuel-prices/GASOLINE", "alice");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        assertThat(RateLimitRule.UPDATE_FUEL_PRICE.capacity).isEqualTo(20);
        verify(rateLimiter).isAllowed(
                "/api/v1/admin/stations/abc/fuel-prices/GASOLINE:alice", 20, 60);
    }

    @Test
    void doFilterInternal_shouldReturn429AndBody_whenLimiterDenies() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("POST", "/api/v1/admin/stations", "alice");
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
    void doFilterInternal_shouldUsePrincipalName_whenPrincipalPresent() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("POST", "/api/v1/admin/stations", "alice");
        request.setRemoteAddr("10.0.0.7");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        verify(rateLimiter).isAllowed(
                "/api/v1/admin/stations:alice", 5, 600);
    }

    @Test
    void doFilterInternal_shouldUseRemoteAddr_whenPrincipalAbsent() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("POST", "/api/v1/admin/stations", null);
        request.setRemoteAddr("10.0.0.7");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        verify(rateLimiter).isAllowed(
                "/api/v1/admin/stations:10.0.0.7", 5, 600);
    }

    @Test
    void doFilterInternal_shouldUseLocalhost_whenRemoteAddrIsIpv6Loopback() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("POST", "/api/v1/admin/stations", null);
        request.setRemoteAddr("::1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        verify(rateLimiter).isAllowed(
                "/api/v1/admin/stations:localhost", 5, 600);
    }

    @Test
    void doFilterInternal_shouldUseLocalhost_whenRemoteAddrIsExpandedIpv6Loopback() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("POST", "/api/v1/admin/stations", null);
        request.setRemoteAddr("0:0:0:0:0:0:0:1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        verify(rateLimiter).isAllowed(
                "/api/v1/admin/stations:localhost", 5, 600);
    }
}