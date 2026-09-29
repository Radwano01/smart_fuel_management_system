package com.example.smart_fuel_management_system.entity;

import com.example.smart_fuel_management_system.enums.AggregateType;
import com.example.smart_fuel_management_system.enums.EventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    private UUID id = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    private AggregateType aggregateType;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private boolean published = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public OutboxEvent(AggregateType aggregateType,
                       EventType eventType,
                       String payload) {
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.payload = payload;
    }
}