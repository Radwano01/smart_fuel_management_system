package com.example.smart_fuel_management_system.service;
import com.example.smart_fuel_management_system.dto.*;
import com.example.smart_fuel_management_system.service.client.StationClient;
import com.example.smart_fuel_management_system.service.client.TransactionClient;
import com.example.smart_fuel_management_system.service.client.UserClient;
import com.example.smart_fuel_management_system.service.client.VehicleClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final VehicleClient vehicleClient;
    private final TransactionClient transactionClient;
    private final UserClient userClient;
    private final StationClient stationClient;

    @Override
    public DashboardAdminResponse getDashboard() {

        VehicleDashboardSummaryResponse vehicles =
                vehicleClient.getVehicleSummary()
                        .orElse(null);

        TransactionDashboardSummaryResponse transactions =
                transactionClient.getDashboardSummary()
                        .orElse(null);

        UserDashboardSummaryResponse users =
                userClient.getUserSummary()
                        .orElse(null);

        StationDashboardSummaryResponse stations =
                stationClient.getStationSummary()
                        .orElse(null);

        return DashboardAdminResponse.builder()
                .vehicles(vehicles)
                .transactions(transactions)
                .users(users)
                .stations(stations)
                .build();
    }

    @Override
    public DashboardUserResponse getDashboard(UUID userId) {
        VehicleCountResponse vehicles =
                vehicleClient.getUserVehicleCount(userId)
                        .orElse(null);

        TransactionCountResponse transactions =
                transactionClient.getUserTransactionsCount(userId)
                        .orElse(null);


        return DashboardUserResponse.builder()
                .vehicleCount(vehicles != null ? vehicles.count() : 0L)
                .transactionCount(transactions != null ? transactions.count() : 0L)
                .build();
    }
}