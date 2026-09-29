package com.example.smart_fuel_management_system.dto.station;

import com.example.smart_fuel_management_system.enums.StationStatusType;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record UpdateStationRequest(
        String name,
        String address,
        String city,
        String contactInformation,
        BigDecimal latitude,
        BigDecimal longitude,
        StationStatusType status
) {}