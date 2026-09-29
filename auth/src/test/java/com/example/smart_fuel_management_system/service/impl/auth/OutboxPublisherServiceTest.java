package com.example.smart_fuel_management_system.service.impl.auth;

import com.example.smart_fuel_management_system.entity.OutboxEvent;
import com.example.smart_fuel_management_system.enums.EventType;
import com.example.smart_fuel_management_system.repository.OutBoxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherServiceTest {

    @Mock
    private OutBoxRepository outBoxRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OutboxPublisherService outboxPublisherService;


    @Test
    void publishEvents_shouldPublishUserCreatedEvent() {

        // given
        OutboxEvent event = new OutboxEvent();
        event.setEventType(EventType.USER_CREATED);
        event.setPayload("user-created-payload");
        event.setPublished(false);

        when(outBoxRepository.findByPublishedFalse(
                PageRequest.of(0, 100)
        )).thenReturn(List.of(event));

        // when
        outboxPublisherService.publishEvents();

        // then
        verify(rabbitTemplate)
                .convertAndSend(
                        "auth.exchange",
                        "user.created",
                        "user-created-payload"
                );

        assertThat(event.isPublished())
                .isTrue();

        verify(outBoxRepository)
                .saveAll(List.of(event));
    }


    @Test
    void publishEvents_shouldPublishStationCreatedEvent() {

        // given
        OutboxEvent event = new OutboxEvent();
        event.setEventType(EventType.STATION_CREATED);
        event.setPayload("station-created-payload");
        event.setPublished(false);

        when(outBoxRepository.findByPublishedFalse(
                PageRequest.of(0, 100)
        )).thenReturn(List.of(event));

        // when
        outboxPublisherService.publishEvents();

        // then
        verify(rabbitTemplate)
                .convertAndSend(
                        "auth.exchange",
                        "station.employee.created",
                        "station-created-payload"
                );

        assertThat(event.isPublished())
                .isTrue();

        verify(outBoxRepository)
                .saveAll(List.of(event));
    }


    @Test
    void publishEvents_shouldKeepEventUnpublished_whenPublishingFails() {

        // given
        OutboxEvent event = new OutboxEvent();
        event.setEventType(EventType.USER_CREATED);
        event.setPayload("user-created-payload");
        event.setPublished(false);

        when(outBoxRepository.findByPublishedFalse(
                PageRequest.of(0, 100)
        )).thenReturn(List.of(event));

        doThrow(new RuntimeException("RabbitMQ error"))
                .when(rabbitTemplate)
                .convertAndSend(
                        "auth.exchange",
                        "user.created",
                        "user-created-payload"
                );

        // when
        outboxPublisherService.publishEvents();

        // then
        assertThat(event.isPublished())
                .isFalse();

        verify(rabbitTemplate)
                .convertAndSend(
                        "auth.exchange",
                        "user.created",
                        "user-created-payload"
                );

        verify(outBoxRepository)
                .saveAll(List.of(event));
    }

    @Test
    void publishEvents_shouldPublishOnlySuccessfulEvents_whenOneEventFails() {

        // given
        OutboxEvent successfulEvent = new OutboxEvent();
        successfulEvent.setEventType(EventType.USER_CREATED);
        successfulEvent.setPayload("user-payload");
        successfulEvent.setPublished(false);

        OutboxEvent failedEvent = new OutboxEvent();
        failedEvent.setEventType(EventType.STATION_CREATED);
        failedEvent.setPayload("station-payload");
        failedEvent.setPublished(false);

        when(outBoxRepository.findByPublishedFalse(
                PageRequest.of(0, 100)
        )).thenReturn(
                List.of(
                        successfulEvent,
                        failedEvent
                )
        );

        doAnswer(invocation -> {

            String routingKey = invocation.getArgument(1);

            if (routingKey.equals("station.employee.created")) {
                throw new RuntimeException("RabbitMQ error");
            }

            return null;

        }).when(rabbitTemplate)
                .convertAndSend(
                        eq("auth.exchange"),
                        anyString(),
                        anyString()
                );

        // when
        outboxPublisherService.publishEvents();

        // then
        assertThat(successfulEvent.isPublished())
                .isTrue();

        assertThat(failedEvent.isPublished())
                .isFalse();

        verify(rabbitTemplate)
                .convertAndSend(
                        "auth.exchange",
                        "user.created",
                        "user-payload"
                );

        verify(rabbitTemplate)
                .convertAndSend(
                        "auth.exchange",
                        "station.employee.created",
                        "station-payload"
                );

        verify(outBoxRepository)
                .saveAll(
                        List.of(
                                successfulEvent,
                                failedEvent
                        )
                );
    }
}
