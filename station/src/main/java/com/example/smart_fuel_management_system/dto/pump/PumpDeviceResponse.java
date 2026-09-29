package com.example.smart_fuel_management_system.dto.pump;


import com.example.smart_fuel_management_system.enums.DeviceStatusType;

import java.util.UUID;

public record PumpDeviceResponse(
        UUID id,
        String deviceId,
        DeviceStatusType status,
        UUID pumpId

) {
}