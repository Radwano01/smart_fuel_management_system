package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.NotificationEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResetPasswordListenerTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private ResetPasswordListener resetPasswordListener;

    @Test
    void handle_shouldSendResetPasswordEmail() {

        // given
        NotificationEvent event = new NotificationEvent(
                "test@gmail.com",
                "Radwan",
                null,
                "reset-token"
        );

        // when
        resetPasswordListener.handle(event);

        // then
        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertThat(message.getTo())
                .containsExactly("test@gmail.com");

        assertThat(message.getSubject())
                .isEqualTo("🔐 Reset Your Password");

        assertThat(message.getText())
                .contains("Hello, Radwan")
                .contains("We received a request to reset your password.")
                .contains("http://localhost:5173/reset-password?token=reset-token")
                .contains("This link will expire in 10 minutes")
                .contains("If you did not request this, please ignore this email.")
                .contains("Best regards,")
                .contains("Smart Fuel Management System Team");
    }
}