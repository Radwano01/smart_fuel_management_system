package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.pump.PumpResponse;
import com.example.smart_fuel_management_system.enums.PumpConnectionStatusType;
import com.example.smart_fuel_management_system.entity.Pump;
import com.example.smart_fuel_management_system.enums.PumpStatusType;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface PumpHeartbeatService {
    void recordHeartbeat(UUID pumpId, PumpStatusType status);
    PumpResponse enrich(Pump pump);
    PumpConnectionStatusType getConnectionStatus(UUID pumpId);
    OffsetDateTime getLastHeartbeatReceived(UUID pumpId);
}