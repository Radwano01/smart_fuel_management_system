package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.AccountStatusType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record StationEmployeeResponse(
        UUID id,
        String fullName,
        String email,
        String phoneNumber,
        AccountStatusType statusType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}