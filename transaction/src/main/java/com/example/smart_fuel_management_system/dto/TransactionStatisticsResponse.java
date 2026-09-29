package com.example.smart_fuel_management_system.dto;

import java.math.BigDecimal;
import java.util.List;

public record TransactionStatisticsResponse(
        long transactionCount,
        BigDecimal fuelVolume,
        BigDecimal revenue,
        List<FuelTypeSalesResponse> fuelSalesByType
) {}