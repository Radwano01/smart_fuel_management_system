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
class EmailNotificationListenerTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailNotificationListener emailNotificationListener;

    @Test
    void handle_shouldSendEmail() {
        NotificationEvent event = new NotificationEvent("user@example.com", "John Doe", "123456", "token-abc");

        emailNotificationListener.handle(event);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertThat(message.getTo()).containsExactly("user@example.com");
        assertThat(message.getSubject()).isEqualTo("Email OTP");
        assertThat(message.getText()).isEqualTo("OTP: 123456");
    }
}