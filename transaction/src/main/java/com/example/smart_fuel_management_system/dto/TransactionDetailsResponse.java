package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.enums.PaymentStatusType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record TransactionDetailsResponse(
        UUID id,
        BigDecimal amount,
        String currency,
        BigDecimal liters,
        BigDecimal pricePerLiter,
        FuelType fuelType,
        LocalDateTime createdAt,
        PaymentStatusType status,
        VehicleTransactionResponse vehicle,
        StationTransactionResponse station
){};