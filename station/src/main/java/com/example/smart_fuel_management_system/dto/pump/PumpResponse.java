package com.example.smart_fuel_management_system.dto.pump;

import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.enums.PumpConnectionStatusType;
import com.example.smart_fuel_management_system.enums.PumpStatusType;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record PumpResponse(
        UUID id,
        long pumpNumber,
        String deviceId,
        Set<FuelType> fuelTypes,
        PumpStatusType status,
        PumpConnectionStatusType connectionStatus,
        OffsetDateTime lastHeartbeatReceived
) {
}