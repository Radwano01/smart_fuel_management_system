package com.example.smart_fuel_management_system.dto.station;

import java.util.UUID;

public record TransactionStationResponse(UUID stationId,
                                         long transactionsCount,
                                         long vehiclesCount) {
}
