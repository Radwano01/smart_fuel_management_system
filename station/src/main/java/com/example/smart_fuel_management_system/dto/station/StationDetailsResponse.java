package com.example.smart_fuel_management_system.dto.station;

import com.example.smart_fuel_management_system.dto.pump.PumpResponse;
import com.example.smart_fuel_management_system.enums.StationStatusType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record StationDetailsResponse(
        UUID id,
        String name,
        String city,
        String address,
        String contactInformation,
        BigDecimal latitude,
        BigDecimal longitude,
        StationStatusType status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        TransactionStationResponse transactionStats,
        StationEmployeeResponse stationAccount,
        List<PumpResponse> pumps
) {}