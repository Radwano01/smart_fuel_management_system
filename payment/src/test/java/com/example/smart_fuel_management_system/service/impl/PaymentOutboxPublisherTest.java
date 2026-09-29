package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.PaymentEvent;
import com.example.smart_fuel_management_system.entity.OutboxEvent;
import com.example.smart_fuel_management_system.repository.PaymentOutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentOutboxPublisherTest {

    @Mock
    private PaymentOutboxRepository repository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PaymentOutboxPublisher paymentOutboxPublisher;

    @Test
    void publishEvents_shouldPublishUnpublishedEvents() throws JsonProcessingException {
        // given
        OutboxEvent event = new OutboxEvent();
        event.setId(UUID.randomUUID());
        event.setEventType("PAYMENT_CREATED");
        event.setAggregateId(UUID.randomUUID());
        event.setPayload("{}");
        event.setPublished(false);

        PaymentEvent paymentEvent = org.mockito.Mockito.mock(PaymentEvent.class);

        when(repository.findByPublishedFalse())
                .thenReturn(List.of(event));

        when(objectMapper.readValue(
                event.getPayload(),
                PaymentEvent.class
        )).thenReturn(paymentEvent);

        // when
        paymentOutboxPublisher.publishEvents();

        // then
        verify(rabbitTemplate).convertAndSend(
                "payment.exchange",
                "transaction.created",
                paymentEvent
        );

        assertThat(event.isPublished()).isTrue();

        verify(repository).save(event);
    }

    @Test
    void publishEvents_shouldPublishAllUnpublishedEvents() throws JsonProcessingException {
        // given
        OutboxEvent firstEvent = new OutboxEvent();
        firstEvent.setPayload("{\"event\":\"first\"}");
        firstEvent.setPublished(false);

        OutboxEvent secondEvent = new OutboxEvent();
        secondEvent.setPayload("{\"event\":\"second\"}");
        secondEvent.setPublished(false);

        PaymentEvent firstPaymentEvent = org.mockito.Mockito.mock(PaymentEvent.class);
        PaymentEvent secondPaymentEvent = org.mockito.Mockito.mock(PaymentEvent.class);

        when(repository.findByPublishedFalse())
                .thenReturn(List.of(firstEvent, secondEvent));

        when(objectMapper.readValue(
                firstEvent.getPayload(),
                PaymentEvent.class
        )).thenReturn(firstPaymentEvent);

        when(objectMapper.readValue(
                secondEvent.getPayload(),
                PaymentEvent.class
        )).thenReturn(secondPaymentEvent);

        // when
        paymentOutboxPublisher.publishEvents();

        // then
        verify(rabbitTemplate).convertAndSend(
                "payment.exchange",
                "transaction.created",
                firstPaymentEvent
        );

        verify(rabbitTemplate).convertAndSend(
                "payment.exchange",
                "transaction.created",
                secondPaymentEvent
        );

        assertThat(firstEvent.isPublished()).isTrue();
        assertThat(secondEvent.isPublished()).isTrue();

        verify(repository).save(firstEvent);
        verify(repository).save(secondEvent);
    }

    @Test
    void publishEvents_shouldDoNothing_whenNoUnpublishedEvents() throws JsonProcessingException {
        // given
        when(repository.findByPublishedFalse())
                .thenReturn(List.of());

        // when
        paymentOutboxPublisher.publishEvents();

        // then
        verify(repository).findByPublishedFalse();
    }
}