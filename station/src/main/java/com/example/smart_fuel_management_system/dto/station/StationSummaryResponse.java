package com.example.smart_fuel_management_system.dto.station;

import com.example.smart_fuel_management_system.enums.StationStatusType;

import java.math.BigDecimal;
import java.util.UUID;

public record StationSummaryResponse(
        UUID id,
        String name,
        String city,
        String address,
        String contactInformation,
        BigDecimal latitude,
        BigDecimal longitude,
        StationStatusType status,
        TransactionStationResponse transactionStats
) {}