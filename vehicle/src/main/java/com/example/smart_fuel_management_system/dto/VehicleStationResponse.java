package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.VehicleStatusType;
import lombok.Builder;

import java.util.UUID;

@Builder
public record VehicleStationResponse(
        UUID id,
        String plateNumber,
        String rfidTag,
        String brand,
        String model,
        int year,
        VehicleStatusType status
) {}