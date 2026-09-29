package com.example.smart_fuel_management_system.entity;


import com.example.smart_fuel_management_system.enums.RoleType;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;
import java.util.UUID;

@RedisHash(value = "pending_user", timeToLive = 300)
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class PendingUser {
    private UUID id;

    private String email;

    private String fullName;

    private String phoneNumber;

    private String password;

    private RoleType role;

    private boolean isVerifiedEmail;

    private boolean isVerifiedPhoneNumber;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}