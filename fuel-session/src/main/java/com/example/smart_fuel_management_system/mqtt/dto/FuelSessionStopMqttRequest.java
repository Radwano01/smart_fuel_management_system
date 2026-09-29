package com.example.smart_fuel_management_system.mqtt.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FuelSessionStopMqttRequest(
        UUID sessionId,
        UUID pumpId,
        BigDecimal liters,
        BigDecimal pricePerLiter,
        BigDecimal totalCost
) {
}
