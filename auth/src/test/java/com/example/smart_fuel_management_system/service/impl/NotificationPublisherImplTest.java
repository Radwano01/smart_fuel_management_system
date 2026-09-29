package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.NotificationEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationPublisherImplTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private NotificationPublisherImpl notificationPublisher;

    @Test
    void sendEmailOtp_shouldPublishEmailNotification() {

        // given
        NotificationEvent notificationEvent = new NotificationEvent(
                "test@gmail.com",
                "Radwan",
                "123456",
                null
        );

        // when
        notificationPublisher.sendEmailOtp(notificationEvent);

        // then
        ArgumentCaptor<NotificationEvent> captor =
                ArgumentCaptor.forClass(NotificationEvent.class);

        verify(rabbitTemplate).convertAndSend(
                eq("auth.exchange"),
                eq("notification.email"),
                captor.capture()
        );

        assertThat(captor.getValue())
                .isEqualTo(notificationEvent);
    }

    @Test
    void sendSmsOtp_shouldPublishSmsNotification() {

        // given
        NotificationEvent notificationEvent = new NotificationEvent(
                "05331234567",
                "Radwan",
                "654321",
                null
        );

        // when
        notificationPublisher.sendSmsOtp(notificationEvent);

        // then
        ArgumentCaptor<NotificationEvent> captor =
                ArgumentCaptor.forClass(NotificationEvent.class);

        verify(rabbitTemplate).convertAndSend(
                eq("auth.exchange"),
                eq("notification.phone.number"),
                captor.capture()
        );

        assertThat(captor.getValue())
                .isEqualTo(notificationEvent);
    }

    @Test
    void sendResetPasswordToken_shouldPublishResetPasswordNotification() {

        // given
        NotificationEvent notificationEvent = new NotificationEvent(
                "test@gmail.com",
                "Radwan",
                null,
                "reset-token"
        );

        // when
        notificationPublisher.sendResetPasswordToken(notificationEvent);

        // then
        ArgumentCaptor<NotificationEvent> captor =
                ArgumentCaptor.forClass(NotificationEvent.class);

        verify(rabbitTemplate).convertAndSend(
                eq("auth.exchange"),
                eq("notification.reset.password"),
                captor.capture()
        );

        assertThat(captor.getValue())
                .isEqualTo(notificationEvent);
    }
}

