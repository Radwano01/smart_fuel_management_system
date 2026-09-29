package com.example.smart_fuel_management_system.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SaveUser(UUID id,
                       String fullName,
                       String email,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
}
