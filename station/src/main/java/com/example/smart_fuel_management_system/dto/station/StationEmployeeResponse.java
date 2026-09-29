package com.example.smart_fuel_management_system.dto.station;

import com.example.smart_fuel_management_system.enums.AccountStatusType;

import java.time.LocalDateTime;
import java.util.UUID;

public record StationEmployeeResponse(
        UUID id,
        String fullName,
        String email,
        String phoneNumber,
        AccountStatusType status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}