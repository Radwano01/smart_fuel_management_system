package com.example.smart_fuel_management_system.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JWTAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JWTAuthenticationFilter filter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest requestWithBearer(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    @Test
    void doFilterInternal_shouldPassThrough_whenAuthorizationHeaderMissing() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_shouldPassThrough_whenAuthorizationHeaderNotBearer() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_shouldClearContextAndContinue_whenTokenIsInvalid() throws Exception {
        // given
        String token = "invalid-token";
        MockHttpServletRequest request = requestWithBearer(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateToken(token)).thenReturn(false);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldAuthenticateAsStation_whenRoleIsStation() throws Exception {
        // given
        String token = "station-token";
        UUID employeeId = UUID.randomUUID();
        MockHttpServletRequest request = requestWithBearer(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractRole(token)).thenReturn("STATION");
        when(jwtService.extractEmployeeId(token)).thenReturn(employeeId);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(employeeId);
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactly("STATION");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldAuthenticateAsAdmin_whenRoleIsAdmin() throws Exception {
        // given
        String token = "admin-token";
        String adminId = "admin-1";
        MockHttpServletRequest request = requestWithBearer(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractRole(token)).thenReturn("ADMIN");
        when(jwtService.extractSubject(token)).thenReturn(adminId);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(adminId);
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactly("ADMIN");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldAuthenticateAsService_whenIssuerIsFuelSessionService() throws Exception {
        // given
        String token = "service-token";
        MockHttpServletRequest request = requestWithBearer(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractRole(token)).thenReturn("SERVICE");
        when(jwtService.extractIssuer(token)).thenReturn("fuel-session-service");
        when(jwtService.extractAudience(token)).thenReturn("station-service");
        when(jwtService.extractSubject(token)).thenReturn("fuel-session-service");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo("fuel-session-service");
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactly("SERVICE");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldAuthenticateAsService_whenIssuerIsTransactionService() throws Exception {
        // given
        String token = "service-token";
        MockHttpServletRequest request = requestWithBearer(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractRole(token)).thenReturn("SERVICE");
        when(jwtService.extractIssuer(token)).thenReturn("transaction-service");
        when(jwtService.extractAudience(token)).thenReturn("station-service");
        when(jwtService.extractSubject(token)).thenReturn("transaction-service");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactly("SERVICE");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldAuthenticateAsService_whenIssuerIsAuthService() throws Exception {
        // given
        String token = "service-token";
        MockHttpServletRequest request = requestWithBearer(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractRole(token)).thenReturn("SERVICE");
        when(jwtService.extractIssuer(token)).thenReturn("auth-service");
        when(jwtService.extractAudience(token)).thenReturn("station-service");
        when(jwtService.extractSubject(token)).thenReturn("auth-service");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactly("SERVICE");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldAuthenticateAsService_whenIssuerIsDashboardService() throws Exception {
        // given
        String token = "service-token";
        MockHttpServletRequest request = requestWithBearer(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractRole(token)).thenReturn("SERVICE");
        when(jwtService.extractIssuer(token)).thenReturn("dashboard-service");
        when(jwtService.extractAudience(token)).thenReturn("station-service");
        when(jwtService.extractSubject(token)).thenReturn("dashboard-service");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactly("SERVICE");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldClearContextAndContinue_whenServiceIssuerIsNotAllowed() throws Exception {
        // given
        String token = "service-token";
        MockHttpServletRequest request = requestWithBearer(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractRole(token)).thenReturn("SERVICE");
        when(jwtService.extractIssuer(token)).thenReturn("rogue-service");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldClearContextAndContinue_whenServiceAudienceIsWrong() throws Exception {
        // given
        String token = "service-token";
        MockHttpServletRequest request = requestWithBearer(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractRole(token)).thenReturn("SERVICE");
        when(jwtService.extractIssuer(token)).thenReturn("auth-service");
        when(jwtService.extractAudience(token)).thenReturn("some-other-service");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldClearContextAndContinue_whenRoleIsUnknown() throws Exception {
        // given
        String token = "weird-token";
        MockHttpServletRequest request = requestWithBearer(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractRole(token)).thenReturn("MODERATOR");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldClearContextAndContinue_whenJwtServiceThrows() throws Exception {
        // given
        String token = "boom-token";
        MockHttpServletRequest request = requestWithBearer(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateToken(token)).thenThrow(new RuntimeException("jwt failure"));

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldClearPreExistingContext_whenAuthenticationFails() throws Exception {
        // given
        String token = "invalid-token";
        MockHttpServletRequest request = requestWithBearer(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("stale-user", null, List.of()));
        when(jwtService.validateToken(token)).thenReturn(false);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}