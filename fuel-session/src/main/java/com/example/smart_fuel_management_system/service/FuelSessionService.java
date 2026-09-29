package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.FuelSessionDTO;
import com.example.smart_fuel_management_system.dto.FuelSessionStartResponse;
import com.example.smart_fuel_management_system.dto.StartFuelSessionRequest;
import com.example.smart_fuel_management_system.dto.StopFuelSessionDTO;
import jakarta.transaction.Transactional;

import java.util.UUID;

public interface FuelSessionService {
    FuelSessionStartResponse startSession(StartFuelSessionRequest request);
    FuelSessionDTO stopSession(UUID sessionId, StopFuelSessionDTO request);
    void pauseSession(UUID sessionId);
    void resumeSession(UUID sessionId);
}
