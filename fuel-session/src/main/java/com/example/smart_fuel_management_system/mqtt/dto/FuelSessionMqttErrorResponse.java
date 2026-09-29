package com.example.smart_fuel_management_system.mqtt.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FuelSessionMqttErrorResponse(
        String action,
        UUID pumpId,
        UUID sessionId,
        String errorType,
        String message,
        OffsetDateTime timestamp
) {
}
