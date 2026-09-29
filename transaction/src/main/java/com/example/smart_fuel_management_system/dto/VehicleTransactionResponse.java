package com.example.smart_fuel_management_system.dto;

import java.util.UUID;

public record VehicleTransactionResponse(
        UUID id,
        String plateNumber,
        String brand,
        String model,
        Integer year
) {
}
