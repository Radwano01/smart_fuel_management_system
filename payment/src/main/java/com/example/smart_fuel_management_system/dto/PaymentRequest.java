package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.FuelType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(

        @NotNull
        UUID userId,

        @NotNull
        UUID fuelSessionId,

        @NotNull
        UUID vehicleId,

        @NotNull
        UUID stationId,

        @NotNull
        UUID pumpId,

        @NotNull
        FuelType fuelType,

        @NotNull
        @Positive
        BigDecimal estimatedLiters,

        @NotNull
        @Positive
        BigDecimal pricePerLiter,

        @NotNull
        @Positive
        BigDecimal amount,

        @NotNull
        @Pattern(regexp = "^[A-Z]{3}$")
        String currency
) {}