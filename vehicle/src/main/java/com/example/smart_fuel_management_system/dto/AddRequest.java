package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.FuelType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddRequest(

        @NotBlank
        @NotNull
        String plateNumber,

        @NotNull
        String brand,

        @NotNull
        String model,

        @Min(1900)
        @Max(2026)
        int year,

        @NotNull
        FuelType fuelType) {}
