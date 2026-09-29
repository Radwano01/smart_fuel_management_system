package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.FuelStatusType;
import com.example.smart_fuel_management_system.enums.FuelType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record FuelSessionDTO(
        UUID sessionId,
        UUID vehicleId,
        UUID stationId,
        String paymentIntentId,
        FuelType fuelType,
        BigDecimal liters,
        BigDecimal totalCost,
        FuelStatusType status,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {}

