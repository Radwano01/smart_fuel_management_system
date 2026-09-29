package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCreatedConsumer {

    public final UserService userService;

    @RabbitListener(queues = "user.created.queue")
    public void consume(String payload) throws JsonProcessingException {
        userService.create(payload);
    }
}