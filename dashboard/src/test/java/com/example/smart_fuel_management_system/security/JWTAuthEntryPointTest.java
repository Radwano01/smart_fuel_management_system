package com.example.smart_fuel_management_system.security;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JWTAuthEntryPointTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    @InjectMocks
    private JWTAuthEntryPoint entryPoint;

    @Test
    void commence_shouldSendUnauthorizedError() throws Exception {
        // given

        // when
        entryPoint.commence(
                request,
                response,
                authException
        );

        // then
        verify(response)
                .sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Unauthorized"
                );
    }
}
