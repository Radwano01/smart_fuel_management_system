package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.NotificationEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class SmsNotificationListener {

    @RabbitListener(queues = "notification.phone.number.queue")
    public void handle(NotificationEvent event) {
    }
}