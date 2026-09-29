package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.enums.PaymentStatusType;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentEvent(

        UUID paymentId,
        UUID userId,
        UUID fuelSessionId,
        UUID vehicleId,
        UUID stationId,
        UUID pumpId,

        FuelType fuelType,

        BigDecimal liters,
        BigDecimal pricePerLiter,
        BigDecimal amount,

        String currency,

        PaymentStatusType status

) {}