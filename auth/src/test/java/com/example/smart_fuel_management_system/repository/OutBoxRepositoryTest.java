package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.entity.OutboxEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OutBoxRepositoryTest {

    @Autowired
    private OutBoxRepository outBoxRepository;

    @Test
    void findByPublishedFalse_shouldReturnOnlyUnpublishedEvents_orderedByCreatedAt() {

        // given
        OutboxEvent event1 = new OutboxEvent();
        event1.setId(UUID.randomUUID());
        event1.setPublished(false);
        event1.setCreatedAt(LocalDateTime.now().minusMinutes(10));

        OutboxEvent event2 = new OutboxEvent();
        event2.setId(UUID.randomUUID());
        event2.setPublished(false);
        event2.setCreatedAt(LocalDateTime.now());

        OutboxEvent event3 = new OutboxEvent();
        event3.setId(UUID.randomUUID());
        event3.setPublished(true);
        event3.setCreatedAt(LocalDateTime.now());

        outBoxRepository.save(event1);
        outBoxRepository.save(event2);
        outBoxRepository.save(event3);

        // when
        var result = outBoxRepository.findByPublishedFalse(PageRequest.of(0, 10));

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(OutboxEvent::isPublished)
                .containsOnly(false);

        // ensure ordering (oldest first)
        assertThat(result.get(0).getCreatedAt())
                .isBeforeOrEqualTo(result.get(1).getCreatedAt());
    }

    @Test
    void findByPublishedFalse_shouldReturnEmpty_whenAllEventsArePublished() {

        // given
        OutboxEvent event = new OutboxEvent();
        event.setId(UUID.randomUUID());
        event.setPublished(true);
        event.setCreatedAt(LocalDateTime.now());

        outBoxRepository.save(event);

        // when
        List<OutboxEvent> result =
                outBoxRepository.findByPublishedFalse(PageRequest.of(0, 10));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByPublishedFalse_shouldRespectPagination() {

        // given
        for (int i = 0; i < 5; i++) {
            OutboxEvent event = new OutboxEvent();
            event.setId(UUID.randomUUID());
            event.setPublished(false);
            event.setCreatedAt(LocalDateTime.now().minusMinutes(10L - i));

            outBoxRepository.save(event);
        }

        // when
        List<OutboxEvent> result =
                outBoxRepository.findByPublishedFalse(PageRequest.of(0, 2));

        // then
        assertThat(result).hasSize(2);
    }
}