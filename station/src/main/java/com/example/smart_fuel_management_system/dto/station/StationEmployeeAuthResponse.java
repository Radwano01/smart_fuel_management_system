package com.example.smart_fuel_management_system.dto.station;

import lombok.Builder;

import java.util.UUID;

@Builder
public record StationEmployeeAuthResponse(
        UUID id,
        String name,
        String city,
        String address
) {
}