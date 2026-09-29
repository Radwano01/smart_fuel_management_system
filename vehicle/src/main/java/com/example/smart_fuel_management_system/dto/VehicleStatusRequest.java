package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.VehicleStatusType;
import jakarta.validation.constraints.NotNull;

public record VehicleStatusRequest(
        @NotNull VehicleStatusType status
) {
}