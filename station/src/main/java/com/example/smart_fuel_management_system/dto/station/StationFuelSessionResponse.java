package com.example.smart_fuel_management_system.dto.station;

import java.util.UUID;

public record StationFuelSessionResponse(UUID stationId,
                                         UUID pumpId) {
}
