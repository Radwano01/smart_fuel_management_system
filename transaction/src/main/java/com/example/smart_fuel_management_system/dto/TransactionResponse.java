package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.enums.PaymentStatusType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID pumpId,
        FuelType fuelType,
        BigDecimal liters,
        BigDecimal pricePerLiter,
        BigDecimal amount,
        LocalDateTime createdAt,
        PaymentStatusType status,
        VehicleTransactionResponse vehicle,
        StationTransactionResponse station
) {
}