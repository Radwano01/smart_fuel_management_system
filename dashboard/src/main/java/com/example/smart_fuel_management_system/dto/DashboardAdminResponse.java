package com.example.smart_fuel_management_system.dto;

import lombok.Builder;

@Builder
public record DashboardAdminResponse(
        VehicleDashboardSummaryResponse vehicles,
        TransactionDashboardSummaryResponse transactions,
        UserDashboardSummaryResponse users,
        StationDashboardSummaryResponse stations
) {}