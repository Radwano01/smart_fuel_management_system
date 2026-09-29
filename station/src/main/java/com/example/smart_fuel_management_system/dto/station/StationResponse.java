package com.example.smart_fuel_management_system.dto.station;

import com.example.smart_fuel_management_system.dto.pump.PumpResponse;
import com.example.smart_fuel_management_system.enums.StationStatusType;

import java.util.List;
import java.util.UUID;

public record StationResponse(UUID id,
                              String name,
                              String city,
                              String address,
                              StationStatusType stationStatusType,
                              List<PumpResponse> pumps) {
}
