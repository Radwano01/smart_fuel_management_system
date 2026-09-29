package com.example.smart_fuel_management_system.service.impl.auth;

import com.example.smart_fuel_management_system.entity.OutboxEvent;
import com.example.smart_fuel_management_system.enums.EventType;
import com.example.smart_fuel_management_system.repository.OutBoxRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class OutboxPublisherService {

    private final OutBoxRepository outBoxRepository;
    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "auth.exchange";

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishEvents() {

        List<OutboxEvent> events =
                outBoxRepository.findByPublishedFalse(PageRequest.of(0, 100));

        for (OutboxEvent event : events) {

            try {
                String routingKey = getRoutingKey(event.getEventType());

                rabbitTemplate.convertAndSend(
                        EXCHANGE,
                        routingKey,
                        event.getPayload()
                );

                event.setPublished(true);

            } catch (Exception e) {
                System.out.println(
                        "Failed to publish outbox event: " + event.getId()
                );
            }
        }

        outBoxRepository.saveAll(events);
    }

    private String getRoutingKey(EventType eventType) {

        return switch (eventType) {
            case USER_CREATED -> "user.created";
            case STATION_CREATED -> "station.employee.created";
        };
    }
}