package com.example.smart_fuel_management_system.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityConfigTest {

    @Test
    void authenticationManager_shouldReturnConfiguredAuthenticationManager()
            throws Exception {
        // given
        SecurityConfig securityConfig =
                new SecurityConfig(
                        mock(JwtAuthenticationFilter.class),
                        mock(RateLimitingFilter.class),
                        mock(JWTAuthEntryPoint.class)
                );

        AuthenticationConfiguration configuration =
                mock(AuthenticationConfiguration.class);

        AuthenticationManager authenticationManager =
                mock(AuthenticationManager.class);

        when(configuration.getAuthenticationManager())
                .thenReturn(authenticationManager);

        // when
        AuthenticationManager result =
                securityConfig.authenticationManager(configuration);

        // then
        assertThat(result)
                .isSameAs(authenticationManager);
    }
}
