package com.example.smart_fuel_management_system.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityConfigTest {

    @Test
    void passwordEncoder_shouldReturnBCryptPasswordEncoder() {
        // given
        SecurityConfig securityConfig = createSecurityConfig();

        // when
        PasswordEncoder passwordEncoder =
                securityConfig.passwordEncoder();

        // then
        assertThat(passwordEncoder)
                .isInstanceOf(
                        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.class
                );
    }

    @Test
    void passwordEncoder_shouldEncodeAndMatchPassword() {
        // given
        SecurityConfig securityConfig = createSecurityConfig();
        PasswordEncoder passwordEncoder =
                securityConfig.passwordEncoder();
        String password = "password123";

        // when
        String encodedPassword =
                passwordEncoder.encode(password);

        // then
        assertThat(encodedPassword)
                .isNotEqualTo(password);
        assertThat(passwordEncoder.matches(
                password,
                encodedPassword
        )).isTrue();
    }

    @Test
    void authenticationManager_shouldReturnConfiguredAuthenticationManager()
            throws Exception {
        // given
        SecurityConfig securityConfig = createSecurityConfig();
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


    private SecurityConfig createSecurityConfig() {
        JWTAuthEntryPoint jwtAuthEntryPoint =
                mock(JWTAuthEntryPoint.class);

        JWTAuthenticationFilter jwtAuthenticationFilter =
                mock(JWTAuthenticationFilter.class);

        RateLimitingFilter rateLimitingFilter =
                mock(RateLimitingFilter.class);

        return new SecurityConfig(
                jwtAuthEntryPoint,
                jwtAuthenticationFilter,
                rateLimitingFilter
        );
    }
}