package com.example.smart_fuel_management_system.dto.station;

import java.util.UUID;

public record StationTransactionResponse(UUID id,
                                         String name,
                                         String city,
                                         String address) {
}
