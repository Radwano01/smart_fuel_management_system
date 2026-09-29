package com.example.smart_fuel_management_system.mqtt.dto;

import com.example.smart_fuel_management_system.enums.FuelStatusType;
import com.example.smart_fuel_management_system.enums.FuelType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FuelSessionStopMqttResponse(
        UUID pumpId,
        UUID sessionId,
        UUID vehicleId,
        UUID stationId,
        String paymentIntentId,
        FuelType fuelType,
        BigDecimal liters,
        BigDecimal totalCost,
        FuelStatusType status,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {
}
