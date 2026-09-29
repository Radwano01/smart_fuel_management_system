package com.example.smart_fuel_management_system.dto.fuelPrice;

import com.example.smart_fuel_management_system.enums.FuelType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FuelPriceResponse(
        UUID id,
        FuelType fuelType,
        BigDecimal price,
        LocalDateTime createdAt
) {}