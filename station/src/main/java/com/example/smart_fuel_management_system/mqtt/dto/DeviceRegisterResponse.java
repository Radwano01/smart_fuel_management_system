package com.example.smart_fuel_management_system.mqtt.dto;

import java.util.UUID;

public record DeviceRegisterResponse(
        String deviceId,
        UUID pumpId
) {
}