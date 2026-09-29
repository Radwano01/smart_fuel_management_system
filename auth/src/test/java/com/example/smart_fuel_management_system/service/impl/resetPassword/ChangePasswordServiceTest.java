package com.example.smart_fuel_management_system.service.impl.resetPassword;

import com.example.smart_fuel_management_system.dto.ChangePasswordRequest;
import com.example.smart_fuel_management_system.entity.Auth;
import com.example.smart_fuel_management_system.exceptions.BadRequestException;
import com.example.smart_fuel_management_system.repository.AuthRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangePasswordServiceTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ChangePasswordService changePasswordService;

    @Test
    void changePassword_shouldThrowException_whenUserDoesNotExist() {

        // given
        UUID userId = UUID.randomUUID();

        ChangePasswordRequest request = new ChangePasswordRequest(
                "oldPassword",
                "newPassword"
        );

        when(authRepository.findById(userId))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() ->
                changePasswordService.changePassword(request, userId)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User does not found!");

        verify(authRepository).findById(userId);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void changePassword_shouldThrowException_whenCurrentPasswordIsIncorrect() {

        // given
        UUID userId = UUID.randomUUID();

        ChangePasswordRequest request = new ChangePasswordRequest(
                "wrongPassword",
                "newPassword"
        );

        Auth user = new Auth();
        user.setPassword("encodedOldPassword");

        when(authRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.currentPassword(),
                user.getPassword()
        )).thenReturn(false);

        // when / then
        assertThatThrownBy(() ->
                changePasswordService.changePassword(request, userId)
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessage("The current password does not match!");

        verify(passwordEncoder).matches(
                request.currentPassword(),
                user.getPassword()
        );

        verify(passwordEncoder, never())
                .encode(anyString());
    }

    @Test
    void changePassword_shouldThrowException_whenNewPasswordIsSameAsCurrent() {

        // given
        UUID userId = UUID.randomUUID();

        ChangePasswordRequest request = new ChangePasswordRequest(
                "oldPassword",
                "oldPassword"
        );

        Auth user = new Auth();
        user.setPassword("encodedOldPassword");

        when(authRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "oldPassword",
                "encodedOldPassword"
        )).thenReturn(true);

        // when / then
        assertThatThrownBy(() ->
                changePasswordService.changePassword(request, userId)
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessage("The password must be different from the current one!");

        verify(passwordEncoder, times(2)).matches(
                "oldPassword",
                "encodedOldPassword"
        );

        verify(passwordEncoder, never())
                .encode(anyString());
    }

    @Test
    void changePassword_shouldUpdatePassword_whenRequestIsValid() {

        // given
        UUID userId = UUID.randomUUID();

        ChangePasswordRequest request = new ChangePasswordRequest(
                "oldPassword",
                "newPassword"
        );

        Auth user = new Auth();
        user.setPassword("encodedOldPassword");

        when(authRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.currentPassword(),
                user.getPassword()
        )).thenReturn(true);

        when(passwordEncoder.matches(
                request.newPassword(),
                user.getPassword()
        )).thenReturn(false);

        when(passwordEncoder.encode(request.newPassword()))
                .thenReturn("encodedNewPassword");

        // when
        changePasswordService.changePassword(request, userId);

        // then
        assertThat(user.getPassword())
                .isEqualTo("encodedNewPassword");

        verify(passwordEncoder).encode(request.newPassword());
    }
}