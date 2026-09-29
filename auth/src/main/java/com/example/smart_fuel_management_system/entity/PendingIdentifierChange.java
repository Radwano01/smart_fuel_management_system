package com.example.smart_fuel_management_system.entity;

import com.example.smart_fuel_management_system.enums.OtpType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;
import java.util.UUID;

@RedisHash(value = "pending_identifier_change", timeToLive = 300)
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class PendingIdentifierChange {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID authId;

    private String fullName;

    @Enumerated(EnumType.STRING)
    private OtpType type;

    private String newIdentifier;

    private boolean isVerified;

    @CreationTimestamp
    private LocalDateTime createdAt;
}