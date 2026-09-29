package com.example.smart_fuel_management_system.dto;


import java.util.List;

public record VehicleDashboardSummaryResponse(
        long totalVehicles,
        long activeVehicles,
        List<RecentVehicleResponse> recentVehicles
) {}