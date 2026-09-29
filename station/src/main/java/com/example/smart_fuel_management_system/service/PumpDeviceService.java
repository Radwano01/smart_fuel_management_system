package com.example.smart_fuel_management_system.service;



import com.example.smart_fuel_management_system.dto.pump.PumpDeviceResponse;

import java.util.UUID;

public interface PumpDeviceService {
    UUID registerDevice(String deviceId);
    PumpDeviceResponse getByDeviceId(String deviceId);
    PumpDeviceResponse assignToPump(
            UUID pumpId,
            String deviceId
    );
    void unassignFromPump(UUID pumpId);
}