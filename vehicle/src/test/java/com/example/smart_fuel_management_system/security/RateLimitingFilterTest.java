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
    void doFilterInternal_shouldPassThrough_whenMethodDoesNotMatchAnyRule() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("PUT", "/api/v1/vehicles", "alice");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter().doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(rateLimiter);
    }

    @Test
    void doFilterInternal_shouldUseCreateVehicleRule_whenPostOnVehicles() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("POST", "/api/v1/vehicles", "alice");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        assertThat(RateLimitRule.CREATE_VEHICLE.capacity).isEqualTo(10);
        verify(rateLimiter).isAllowed(
                "PUBLIC:/api/v1/vehicles:alice", 10, 60);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldUseListVehiclesRule_whenGetOnVehicles() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("GET", "/api/v1/vehicles", "alice");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        assertThat(RateLimitRule.LIST_VEHICLES.capacity).isEqualTo(30);
        verify(rateLimiter).isAllowed(
                "PUBLIC:/api/v1/vehicles:alice", 30, 60);
    }

    @Test
    void doFilterInternal_shouldUseUpdateVehicleRule_whenPatchOnVehicles() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("PATCH", "/api/v1/vehicles", "alice");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        assertThat(RateLimitRule.UPDATE_VEHICLE.capacity).isEqualTo(10);
        verify(rateLimiter).isAllowed(
                "PUBLIC:/api/v1/vehicles:alice", 10, 60);
    }

    @Test
    void doFilterInternal_shouldUseActivateVehicleRule_whenPathEndsWithActivate() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("PATCH", "/api/v1/vehicles/abc/activate", "alice");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        assertThat(RateLimitRule.ACTIVATE_VEHICLE.capacity).isEqualTo(10);
        verify(rateLimiter).isAllowed(
                "PUBLIC:/api/v1/vehicles/abc/activate:alice", 10, 60);
    }

    @Test
    void doFilterInternal_shouldUseDeactivateVehicleRule_whenPathEndsWithDeactivate() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("PATCH", "/api/v1/vehicles/abc/deactivate", "alice");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        assertThat(RateLimitRule.DEACTIVATE_VEHICLE.capacity).isEqualTo(10);
        verify(rateLimiter).isAllowed(
                "PUBLIC:/api/v1/vehicles/abc/deactivate:alice", 10, 60);
    }

    @Test
    void doFilterInternal_shouldUseFindByRfidRule_whenGetOnAdminRfidByValue() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("GET", "/api/v1/admin/vehicles/rfid/RFID-1", "admin-1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        assertThat(RateLimitRule.FIND_BY_RFID.capacity).isEqualTo(60);
        verify(rateLimiter).isAllowed(
                "ADMIN:/api/v1/admin/vehicles/rfid/RFID-1:admin-1", 60, 60);
    }

    @Test
    void doFilterInternal_shouldUseRemoveRfidRule_whenDeleteOnAdminRfidByValue() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("DELETE", "/api/v1/admin/vehicles/rfid/RFID-1", "admin-1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        assertThat(RateLimitRule.REMOVE_RFID.capacity).isEqualTo(5);
        verify(rateLimiter).isAllowed(
                "ADMIN:/api/v1/admin/vehicles/rfid/RFID-1:admin-1", 5, 60);
    }

    @Test
    void doFilterInternal_shouldUseAdminVehicleStatusRule_whenPatchOnAdminVehicleStatus() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("PATCH", "/api/v1/admin/vehicles/abc/status", "admin-1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        assertThat(RateLimitRule.ADMIN_VEHICLE_STATUS.capacity).isEqualTo(10);
        verify(rateLimiter).isAllowed(
                "ADMIN:/api/v1/admin/vehicles/abc/status:admin-1", 10, 60);
    }

    @Test
    void doFilterInternal_shouldUseAdminVehicleLookupRule_whenGetOnAdminVehiclePlate() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("GET", "/api/v1/admin/vehicles/plate/34ABC123", "admin-1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        assertThat(RateLimitRule.ADMIN_VEHICLE_LOOKUP.capacity).isEqualTo(30);
        verify(rateLimiter).isAllowed(
                "ADMIN:/api/v1/admin/vehicles/plate/34ABC123:admin-1", 30, 60);
    }

    @Test
    void doFilterInternal_shouldUseAdminVehicleDeleteRule_whenDeleteOnAdminVehicleById() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("DELETE", "/api/v1/admin/vehicles/abc", "admin-1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        assertThat(RateLimitRule.ADMIN_VEHICLE_DELETE.capacity).isEqualTo(5);
        verify(rateLimiter).isAllowed(
                "ADMIN:/api/v1/admin/vehicles/abc:admin-1", 5, 60);
    }

    @Test
    void doFilterInternal_shouldUseAdminVehicleRfidAssignRule_whenPatchOnAdminVehicleRfid() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("PATCH", "/api/v1/admin/vehicles/abc/rfid", "admin-1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        assertThat(RateLimitRule.ADMIN_VEHICLE_RFID_ASSIGN.capacity).isEqualTo(5);
        verify(rateLimiter).isAllowed(
                "ADMIN:/api/v1/admin/vehicles/abc/rfid:admin-1", 5, 60);
    }

    @Test
    void doFilterInternal_shouldUsePublicScope_whenPathIsNotAdminVehicles() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("GET", "/api/v1/vehicles", "alice");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        verify(rateLimiter).isAllowed(
                "PUBLIC:/api/v1/vehicles:alice", 30, 60);
    }

    @Test
    void doFilterInternal_shouldUseAdminScope_whenPathIsAdminVehicles() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("GET", "/api/v1/admin/vehicles/rfid/RFID-1", "admin-1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        verify(rateLimiter).isAllowed(
                "ADMIN:/api/v1/admin/vehicles/rfid/RFID-1:admin-1", 60, 60);
    }

    @Test
    void doFilterInternal_shouldUsePrincipalName_whenPrincipalPresent() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("GET", "/api/v1/vehicles", "alice");
        request.setRemoteAddr("10.0.0.7");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        verify(rateLimiter).isAllowed(
                "PUBLIC:/api/v1/vehicles:alice", 30, 60);
    }

    @Test
    void doFilterInternal_shouldUseRemoteAddr_whenPrincipalAbsent() throws Exception {
        // given
        MockHttpServletRequest request = requestFor("GET", "/api/v1/vehicles", null);
        request.setRemoteAddr("10.0.0.7");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        verify(rateLimiter).isAllowed(
                "PUBLIC:/api/v1/vehicles:10.0.0.7", 30, 60);
    }

    @Test
    void doFilterInternal_shouldReturn429AndBody_whenLimiterDenies() throws Exception {
        // given
        MockHttpServletRequest request =
                requestFor("GET", "/api/v1/vehicles", "alice");
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
        MockHttpServletRequest request =
                requestFor("GET", "/api/v1/vehicles", "alice");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);

        // when
        filter().doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
    }
}