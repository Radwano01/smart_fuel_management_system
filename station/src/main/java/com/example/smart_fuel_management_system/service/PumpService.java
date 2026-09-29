package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.pump.CreatePumpRequest;
import com.example.smart_fuel_management_system.dto.pump.PumpResponse;
import com.example.smart_fuel_management_system.dto.station.StationFuelSessionResponse;

import java.util.UUID;

public interface PumpService {
    PumpResponse create(UUID stationId, CreatePumpRequest request);
    StationFuelSessionResponse getStationId(UUID id);
}