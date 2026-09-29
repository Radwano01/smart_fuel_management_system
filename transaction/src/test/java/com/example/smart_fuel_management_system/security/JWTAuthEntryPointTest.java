package com.example.smart_fuel_management_system.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class JWTAuthEntryPointTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private JWTAuthEntryPoint entryPoint;

    @Test
    void commence_sendsUnauthorizedWithMessage() throws Exception {
        // given
        AuthenticationException authException = mock(AuthenticationException.class);

        // when
        entryPoint.commence(request, response, authException);

        // then
        verify(response).sendError(
                eq(HttpServletResponse.SC_UNAUTHORIZED),
                eq("Unauthorized"));
        verifyNoMoreInteractions(response);
    }

    @Test
    void commence_ignoresRequestAndException() throws Exception {
        // given
        AuthenticationException authException = mock(AuthenticationException.class);

        // when
        entryPoint.commence(request, response, authException);

        // then
        verify(request, org.mockito.Mockito.never()).getHeader(org.mockito.ArgumentMatchers.anyString());
    }
}