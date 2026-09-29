package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.FuelType;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record FuelTypeSalesResponse(
        FuelType fuelType,
        BigDecimal fuelVolume,
        BigDecimal revenue
) {}