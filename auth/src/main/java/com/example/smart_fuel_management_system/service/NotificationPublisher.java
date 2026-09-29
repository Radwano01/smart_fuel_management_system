package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.NotificationEvent;

public interface NotificationPublisher {
    void sendEmailOtp(NotificationEvent notificationEvent);

    void sendSmsOtp(NotificationEvent notificationEvent);

    void sendResetPasswordToken(NotificationEvent notificationEvent);
}
