package com.example.smart_fuel_management_system.mqtt.dto;

import com.example.smart_fuel_management_system.enums.PumpStatusType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PumpHeartbeatMqttRequest(
        UUID pumpId,
        PumpStatusType status,
        OffsetDateTime timestamp
) {
}