package com.example.smart_fuel_management_system.dto.pump;

import jakarta.validation.constraints.NotBlank;

public record AssignPumpDeviceRequest(

        @NotBlank(message = "Device ID is required")
        String deviceId

) {
}