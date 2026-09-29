package com.example.smart_fuel_management_system.service.impl.resetPassword;

import com.example.smart_fuel_management_system.entity.PasswordResetToken;
import com.example.smart_fuel_management_system.exceptions.BadRequestException;
import com.example.smart_fuel_management_system.repository.PasswordResetTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetTokenServiceTest {

    @Mock
    private PasswordResetTokenRepository repository;

    @InjectMocks
    private PasswordResetTokenService passwordResetTokenService;

    @Test
    void createToken_shouldDeleteExistingTokenAndSaveNewToken() {

        // given
        UUID userId = UUID.randomUUID();

        PasswordResetToken savedToken = new PasswordResetToken();
        savedToken.setToken("generated-token");
        savedToken.setUserId(userId);
        savedToken.setExpireDate(LocalDateTime.now().plusMinutes(10));

        when(repository.save(any(PasswordResetToken.class)))
                .thenReturn(savedToken);

        // when
        PasswordResetToken result =
                passwordResetTokenService.createToken(userId);

        // then
        assertThat(result).isEqualTo(savedToken);

        ArgumentCaptor<PasswordResetToken> captor =
                ArgumentCaptor.forClass(PasswordResetToken.class);

        verify(repository).deleteByUserId(userId);
        verify(repository).save(captor.capture());

        PasswordResetToken token = captor.getValue();

        assertThat(token.getUserId()).isEqualTo(userId);
        assertThat(token.getToken()).isNotBlank();
        assertThat(token.getExpireDate())
                .isAfter(LocalDateTime.now());
        assertThat(token.getExpireDate())
                .isBefore(LocalDateTime.now().plusMinutes(11));
    }

    @Test
    void validateToken_shouldReturnUserIdAndDeleteToken_whenTokenIsValid() {

        // given
        String token = "valid-token";
        UUID userId = UUID.randomUUID();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUserId(userId);
        resetToken.setExpireDate(LocalDateTime.now().plusMinutes(5));

        when(repository.findByToken(token))
                .thenReturn(Optional.of(resetToken));

        // when
        UUID result = passwordResetTokenService.validateToken(token);

        // then
        assertThat(result).isEqualTo(userId);

        verify(repository).findByToken(token);
        verify(repository).delete(resetToken);
    }

    @Test
    void validateToken_shouldThrowException_whenTokenDoesNotExist() {

        // given
        String token = "invalid-token";

        when(repository.findByToken(token))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() ->
                passwordResetTokenService.validateToken(token)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Token not found or expired");

        verify(repository).findByToken(token);
        verify(repository, never()).delete(any());
    }

    @Test
    void validateToken_shouldThrowException_whenTokenIsExpired() {

        // given
        String token = "expired-token";

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUserId(UUID.randomUUID());
        resetToken.setExpireDate(LocalDateTime.now().minusMinutes(1));

        when(repository.findByToken(token))
                .thenReturn(Optional.of(resetToken));

        // when / then
        assertThatThrownBy(() ->
                passwordResetTokenService.validateToken(token)
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Token expired");

        verify(repository).findByToken(token);
        verify(repository, never()).delete(any());
    }
}
