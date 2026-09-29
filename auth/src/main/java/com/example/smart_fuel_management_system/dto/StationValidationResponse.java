package com.example.smart_fuel_management_system.dto;

public record StationValidationResponse(
        boolean stationExists,
        boolean hasAccount
) {
}
