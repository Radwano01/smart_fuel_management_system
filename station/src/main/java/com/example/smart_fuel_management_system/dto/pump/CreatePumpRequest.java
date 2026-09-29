package com.example.smart_fuel_management_system.dto.pump;

import com.example.smart_fuel_management_system.enums.FuelType;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record CreatePumpRequest(
        @NotEmpty
        Set<FuelType> fuelTypes
) {
}