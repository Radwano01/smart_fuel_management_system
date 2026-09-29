package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.PaymentEvent;
import com.example.smart_fuel_management_system.entity.OutboxEvent;
import com.example.smart_fuel_management_system.repository.PaymentOutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentOutboxPublisher {

    private final PaymentOutboxRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishEvents() throws JsonProcessingException {

        List<OutboxEvent> events =
                repository.findByPublishedFalse();

        for (OutboxEvent event : events) {

            PaymentEvent paymentEvent =
                    objectMapper.readValue(event.getPayload(), PaymentEvent.class);

            rabbitTemplate.convertAndSend(
                    "payment.exchange",
                    "transaction.created",
                    paymentEvent
            );

            event.setPublished(true);
            repository.save(event);
        }
    }
}