package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.enums.VehicleStatusType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record VehicleDTO(UUID id,
                         String plateNumber,
                         String rfidTag,
                         String brand,
                         String model,
                         int year,
                         FuelType fuelType,
                         VehicleStatusType status,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt) {}
