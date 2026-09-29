package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.FuelType;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
        UUID userId,
        UUID fuelSessionId,
        UUID vehicleId,
        UUID stationId,
        UUID pumpId,
        FuelType fuelType,
        BigDecimal estimatedLiters,
        BigDecimal pricePerLiter,
        BigDecimal amount,
        String currency
) {}