package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.FuelStatusType;
import com.example.smart_fuel_management_system.enums.FuelType;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record FuelSessionStartResponse(
        UUID sessionId,
        FuelStatusType status,
        boolean allowed,
        FuelType fuelType,
        BigDecimal authorizedAmount
) {}