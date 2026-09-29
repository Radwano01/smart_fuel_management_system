package com.example.smart_fuel_management_system.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @MockBean
    private JWTAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JWTAuthEntryPoint jwtAuthEntryPoint;

    @MockBean
    private RateLimitingFilter rateLimitingFilter;

    @Test
    void securityFilterChain_shouldReturnConfiguredSecurityFilterChain() {

        // when
        SecurityFilterChain result = securityFilterChain;

        // then
        assertThat(result).isNotNull();
    }
}
