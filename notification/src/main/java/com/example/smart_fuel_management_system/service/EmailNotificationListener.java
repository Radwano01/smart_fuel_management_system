package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailNotificationListener {

    private final JavaMailSender mailSender;

    @RabbitListener(queues = "notification.email.queue")
    public void handle(NotificationEvent event) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.destination());
        message.setSubject("Email OTP");

        message.setText("OTP: " + event.otp());

        mailSender.send(message);
    }
}