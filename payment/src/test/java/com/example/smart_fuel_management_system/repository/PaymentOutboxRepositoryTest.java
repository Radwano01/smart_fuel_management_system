package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.entity.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class PaymentOutboxRepositoryTest {

    @Autowired
    private PaymentOutboxRepository paymentOutboxRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS outbox_events (
                    id UUID PRIMARY KEY,
                    event_type VARCHAR(255),
                    aggregate_id UUID,
                    payload CLOB,
                    published BOOLEAN,
                    created_at TIMESTAMP
                )
                """);
    }

    @Test
    void findByPublishedFalse_shouldReturnUnpublishedEvents() {

        // given
        OutboxEvent unpublishedEvent = new OutboxEvent();
        unpublishedEvent.setEventType("PAYMENT_CREATED");
        unpublishedEvent.setAggregateId(UUID.randomUUID());
        unpublishedEvent.setPayload("{}");
        unpublishedEvent.setPublished(false);

        OutboxEvent publishedEvent = new OutboxEvent();
        publishedEvent.setEventType("PAYMENT_COMPLETED");
        publishedEvent.setAggregateId(UUID.randomUUID());
        publishedEvent.setPayload("{}");
        publishedEvent.setPublished(true);

        paymentOutboxRepository.save(unpublishedEvent);
        paymentOutboxRepository.saveAndFlush(publishedEvent);

        // when
        var result = paymentOutboxRepository.findByPublishedFalse();

        // then
        assertThat(result)
                .hasSize(1)
                .containsExactly(unpublishedEvent);
    }

    @Test
    void findByPublishedFalse_shouldReturnEmpty_whenAllEventsArePublished() {

        // given
        OutboxEvent event = new OutboxEvent();
        event.setEventType("PAYMENT_CREATED");
        event.setAggregateId(UUID.randomUUID());
        event.setPayload("{}");
        event.setPublished(true);

        paymentOutboxRepository.saveAndFlush(event);

        // when
        var result = paymentOutboxRepository.findByPublishedFalse();

        // then
        assertThat(result).isEmpty();
    }
}
