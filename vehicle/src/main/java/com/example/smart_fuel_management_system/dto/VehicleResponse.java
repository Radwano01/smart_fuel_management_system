package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.enums.VehicleStatusType;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

// THIS DTO CREATED TO SERVE FUEL SESSIONS SERVICE
@Builder
public record VehicleResponse(UUID userId,
                              UUID vehicleId,
                              FuelType fuelType,
                              VehicleStatusType vehicleStatusType,
                              BigDecimal tankCapacity) {
}