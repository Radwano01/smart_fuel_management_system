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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
    void noAuthorizationHeader_passesThrough() throws Exception {
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
    void nonBearerHeader_passesThrough() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void invalidToken_clearsContextAndContinues() throws Exception {
        // given
        String token = "invalid";
        MockHttpServletRequest request = requestWithBearer(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateToken(token)).thenReturn(false);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void adminToken_setsAuthentication() throws Exception {
        // given
        String token = "admin-token";
        String subject = "admin-user";
        MockHttpServletRequest request = requestWithBearer(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractRole(token)).thenReturn("ADMIN");
        when(jwtService.extractSubject(token)).thenReturn(subject);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(subject);
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactly("ADMIN");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void userToken_setsAuthenticationWithUserId() throws Exception {
        // given
        String token = "user-token";
        UUID userId = UUID.randomUUID();
        MockHttpServletRequest request = requestWithBearer(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractRole(token)).thenReturn("USER");
        when(jwtService.extractUserId(token)).thenReturn(userId);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(userId);
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactly("USER");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void serviceToken_validIssuerAndAudience_setsAuthentication() throws Exception {
        // given
        String token = "service-token";
        String subject = "dashboard-service";
        MockHttpServletRequest request = requestWithBearer(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractRole(token)).thenReturn("SERVICE");
        when(jwtService.extractSubject(token)).thenReturn(subject);
        when(jwtService.extractIssuer(token)).thenReturn("dashboard-service");
        when(jwtService.extractAudience(token)).thenReturn("transaction-service");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(subject);
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactly("SERVICE");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void serviceToken_invalidIssuer_clearsContext() throws Exception {
        // given
        String token = "service-token";
        MockHttpServletRequest request = requestWithBearer(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractRole(token)).thenReturn("SERVICE");
        when(jwtService.extractSubject(token)).thenReturn("admin-service");
        when(jwtService.extractIssuer(token)).thenReturn("some-other-service");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void serviceToken_invalidAudience_clearsContext() throws Exception {
        // given
        String token = "service-token";
        MockHttpServletRequest request = requestWithBearer(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractRole(token)).thenReturn("SERVICE");
        when(jwtService.extractSubject(token)).thenReturn("dashboard-service");
        when(jwtService.extractIssuer(token)).thenReturn("dashboard-service");
        when(jwtService.extractAudience(token)).thenReturn("wrong-service");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void unknownRole_clearsContextAndContinues() throws Exception {
        // given
        String token = "weird-token";
        MockHttpServletRequest request = requestWithBearer(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractRole(token)).thenReturn("UNKNOWN");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void jwtServiceThrows_clearsContextAndContinues() throws Exception {
        // given
        String token = "boom";
        MockHttpServletRequest request = requestWithBearer(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateToken(token)).thenThrow(new RuntimeException("jwt error"));

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}