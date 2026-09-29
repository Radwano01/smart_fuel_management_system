package com.example.smart_fuel_management_system.mqtt.dto;

import com.example.smart_fuel_management_system.enums.FuelStatusType;

import java.util.UUID;

public record FuelSessionPauseMqttResponse(
        UUID sessionId,
        UUID pumpId,
        FuelStatusType status
) {
}