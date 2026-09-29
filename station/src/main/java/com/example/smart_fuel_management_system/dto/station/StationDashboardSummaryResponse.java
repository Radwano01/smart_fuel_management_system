package com.example.smart_fuel_management_system.dto.station;

public record StationDashboardSummaryResponse(
        long totalStations,
        long totalStationAccounts,
        long activeStations,
        long inactiveStations,
        long maintenanceStations
) {}