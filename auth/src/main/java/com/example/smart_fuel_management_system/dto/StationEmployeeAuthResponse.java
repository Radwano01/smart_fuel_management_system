package com.example.smart_fuel_management_system.dto;

import java.util.UUID;

public record StationEmployeeAuthResponse(
        UUID id,
        String name,
        String city,
        String address
) {
}
