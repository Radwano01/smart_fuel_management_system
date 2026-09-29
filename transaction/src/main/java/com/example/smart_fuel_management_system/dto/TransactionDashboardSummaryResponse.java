package com.example.smart_fuel_management_system.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record TransactionDashboardSummaryResponse(
        long transactionsCount,
        long todayTransactions,
        BigDecimal todayFuelVolume,
        BigDecimal todayRevenue,
        List<FuelTypeSalesResponse> fuelSalesByType,
        List<TransactionSummaryResponse> recentTransactions
) {}