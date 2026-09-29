package com.example.smart_fuel_management_system.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record StopFuelSessionDTO(BigDecimal liters) {
}
