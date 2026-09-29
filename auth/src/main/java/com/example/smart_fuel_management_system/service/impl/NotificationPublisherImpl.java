package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.NotificationEvent;
import com.example.smart_fuel_management_system.service.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationPublisherImpl implements NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendEmailOtp(NotificationEvent notificationEvent) {
        rabbitTemplate.convertAndSend(
                "auth.exchange",
                "notification.email",
                notificationEvent
        );
    }

    @Override
    public void sendSmsOtp(NotificationEvent notificationEvent) {
        rabbitTemplate.convertAndSend(
                "auth.exchange",
                "notification.phone.number",
                notificationEvent
        );
    }

    @Override
    public void sendResetPasswordToken(NotificationEvent notificationEvent) {
        rabbitTemplate.convertAndSend(
                "auth.exchange",
                "notification.reset.password",
                notificationEvent
        );
    }
}