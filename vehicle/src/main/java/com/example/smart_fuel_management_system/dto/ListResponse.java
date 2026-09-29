package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.VehicleStatusType;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ListResponse(UUID id,
                      String brand,
                      String model,
                      int year,
                      VehicleStatusType status) {
}
