package com.example.smart_fuel_management_system.mqtt.dto;

import com.example.smart_fuel_management_system.enums.FuelType;

import java.util.UUID;

public record FuelSessionStartMqttRequest(
        String plateNumber,
        String rfidTag,
        UUID stationId,
        UUID pumpId
) {
}
