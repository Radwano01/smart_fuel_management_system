package com.example.smart_fuel_management_system.dto.fuelPrice;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record FuelPriceRequest(
        @NotNull
        @Positive
        BigDecimal price) {}
