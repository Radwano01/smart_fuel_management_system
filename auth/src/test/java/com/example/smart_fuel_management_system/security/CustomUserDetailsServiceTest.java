package com.example.smart_fuel_management_system.security;

import com.example.smart_fuel_management_system.entity.Auth;
import com.example.smart_fuel_management_system.enums.RoleType;
import com.example.smart_fuel_management_system.repository.AuthRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private AuthRepository authRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        // given
        UUID userId = UUID.randomUUID();

        Auth user = new Auth();
        user.setId(userId);
        user.setPassword("encodedPassword");
        user.setRole(RoleType.USER);

        when(authRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // when
        UserDetails result =
                service.loadUserByUsername(userId.toString());

        // then
        assertThat(result.getUsername()).isEqualTo(userId.toString());
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("USER");
    }

    @Test
    void loadUserByUsername_shouldReturnAdminAuthority_whenUserIsAdmin() {
        // given
        UUID userId = UUID.randomUUID();

        Auth user = new Auth();
        user.setId(userId);
        user.setPassword("encodedPassword");
        user.setRole(RoleType.ADMIN);

        when(authRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // when
        UserDetails result =
                service.loadUserByUsername(userId.toString());

        // then
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("ADMIN");
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserDoesNotExist() {
        // given
        UUID userId = UUID.randomUUID();

        when(authRepository.findById(userId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername(userId.toString())
        );
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenIdIsInvalid() {
        // given
        String invalidId = "invalid-uuid";

        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> service.loadUserByUsername(invalidId)
        );
    }
}