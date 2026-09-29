package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.AccountStatusType;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String fullName,
        String email,
        String phoneNumber,
        AccountStatusType statusType,
        LocalDateTime createdAt
) {
}
