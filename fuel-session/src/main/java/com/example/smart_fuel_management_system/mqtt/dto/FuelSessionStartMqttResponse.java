package com.example.smart_fuel_management_system.mqtt.dto;

import com.example.smart_fuel_management_system.enums.FuelStatusType;
import com.example.smart_fuel_management_system.enums.FuelType;

import java.math.BigDecimal;
import java.util.UUID;

public record FuelSessionStartMqttResponse(
        UUID sessionId,
        FuelStatusType status,
        boolean allowed,
        FuelType fuelType,
        BigDecimal authorizedAmount
) {
}
