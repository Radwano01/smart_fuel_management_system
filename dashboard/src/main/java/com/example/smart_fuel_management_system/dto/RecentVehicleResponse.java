package com.example.smart_fuel_management_system.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RecentVehicleResponse(
        UUID id,
        String plateNumber,
        String brand,
        String model,
        LocalDateTime createdAt
) {}