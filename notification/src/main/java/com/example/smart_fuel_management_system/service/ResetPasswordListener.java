package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResetPasswordListener {

    private final JavaMailSender mailSender;

    @RabbitListener(queues = "notification.reset.password.queue")
    public void handle(NotificationEvent event) {

        String link = "http://localhost:5173/reset-password?token=" + event.token();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.destination());
        message.setSubject("🔐 Reset Your Password");

        String emailContent =
                "Hello, " + event.fullName() + "\n\n"
                        + "We received a request to reset your password.\n\n"
                        + "Click the link below to reset it:\n"
                        + link + "\n\n"
                        + "⚠️ This link will expire in 10 minutes for security reasons.\n\n"
                        + "If you did not request this, please ignore this email.\n\n"
                        + "Best regards,\n"
                        + "Smart Fuel Management System Team";

        message.setText(emailContent);

        mailSender.send(message);
    }
}