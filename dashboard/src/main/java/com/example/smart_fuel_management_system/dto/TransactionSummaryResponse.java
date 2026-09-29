package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.FuelType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record TransactionSummaryResponse(
        UUID id,
        BigDecimal amount,
        FuelType fuelType,
        LocalDateTime createdAt
) {}