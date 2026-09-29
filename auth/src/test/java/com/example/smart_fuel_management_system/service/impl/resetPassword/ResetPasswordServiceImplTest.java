package com.example.smart_fuel_management_system.service.impl.resetPassword;

import com.example.smart_fuel_management_system.dto.NotificationEvent;
import com.example.smart_fuel_management_system.dto.ResetPasswordRequest;
import com.example.smart_fuel_management_system.dto.ResetPasswordTokenRequest;
import com.example.smart_fuel_management_system.dto.UserResponse;
import com.example.smart_fuel_management_system.entity.Auth;
import com.example.smart_fuel_management_system.entity.PasswordResetToken;
import com.example.smart_fuel_management_system.repository.AuthRepository;
import com.example.smart_fuel_management_system.service.NotificationPublisher;
import com.example.smart_fuel_management_system.service.impl.auth.client.UserClient;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetPasswordServiceImplTest {

    @Mock
    private PasswordResetTokenService tokenService;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserClient userClient;

    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private ResetPasswordServiceImpl resetPasswordService;

    @Test
    void resetPassword_shouldUpdatePassword_whenTokenAndUserAreValid() {

        // given
        String token = "reset-token";
        UUID userId = UUID.randomUUID();

        ResetPasswordRequest request =
                new ResetPasswordRequest("newPassword");

        Auth user = new Auth();
        user.setId(userId);
        user.setPassword("oldEncodedPassword");

        when(tokenService.validateToken(token))
                .thenReturn(userId);

        when(authRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.encode(request.newPassword()))
                .thenReturn("newEncodedPassword");

        // when
        resetPasswordService.resetPassword(request, token);

        // then
        assertThat(user.getPassword())
                .isEqualTo("newEncodedPassword");

        verify(tokenService).validateToken(token);
        verify(authRepository).findById(userId);
        verify(passwordEncoder).encode(request.newPassword());
    }

    @Test
    void resetPassword_shouldThrowException_whenUserDoesNotExist() {

        // given
        String token = "reset-token";
        UUID userId = UUID.randomUUID();

        ResetPasswordRequest request =
                new ResetPasswordRequest("newPassword");

        when(tokenService.validateToken(token))
                .thenReturn(userId);

        when(authRepository.findById(userId))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() ->
                resetPasswordService.resetPassword(request, token)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("user not found");

        verify(tokenService).validateToken(token);
        verify(authRepository).findById(userId);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void createAndNotifyPasswordResetToken_shouldCreateTokenAndSendNotification() {

        // given
        String email = "test@gmail.com";
        UUID userId = UUID.randomUUID();

        ResetPasswordTokenRequest request =
                new ResetPasswordTokenRequest(email);

        UserResponse user = mock(UserResponse.class);
        when(user.id()).thenReturn(userId);
        when(user.fullName()).thenReturn("Radwan");

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("generated-reset-token");
        resetToken.setUserId(userId);

        when(authRepository.findIdByEmail(email))
                .thenReturn(userId);

        when(userClient.getUserById(userId))
                .thenReturn(user);

        when(tokenService.createToken(userId))
                .thenReturn(resetToken);

        // when
        resetPasswordService.createAndNotifyPasswordResetToken(request);

        // then
        ArgumentCaptor<NotificationEvent> captor =
                ArgumentCaptor.forClass(NotificationEvent.class);

        verify(notificationPublisher).sendResetPasswordToken(
                captor.capture()
        );

        NotificationEvent event = captor.getValue();

        assertThat(event.destination()).isEqualTo(email);
        assertThat(event.fullName()).isEqualTo("Radwan");
        assertThat(event.otp()).isEmpty();
        assertThat(event.token()).isEqualTo("generated-reset-token");
    }
}
