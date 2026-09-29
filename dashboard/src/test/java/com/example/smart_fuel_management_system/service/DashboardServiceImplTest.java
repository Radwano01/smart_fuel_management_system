package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.DashboardAdminResponse;
import com.example.smart_fuel_management_system.dto.DashboardUserResponse;
import com.example.smart_fuel_management_system.dto.StationDashboardSummaryResponse;
import com.example.smart_fuel_management_system.dto.TransactionCountResponse;
import com.example.smart_fuel_management_system.dto.TransactionDashboardSummaryResponse;
import com.example.smart_fuel_management_system.dto.UserDashboardSummaryResponse;
import com.example.smart_fuel_management_system.dto.VehicleCountResponse;
import com.example.smart_fuel_management_system.dto.VehicleDashboardSummaryResponse;
import com.example.smart_fuel_management_system.service.client.StationClient;
import com.example.smart_fuel_management_system.service.client.TransactionClient;
import com.example.smart_fuel_management_system.service.client.UserClient;
import com.example.smart_fuel_management_system.service.client.VehicleClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private VehicleClient vehicleClient;

    @Mock
    private TransactionClient transactionClient;

    @Mock
    private UserClient userClient;

    @Mock
    private StationClient stationClient;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void getDashboard_shouldReturnDashboardSummary_whenAllServicesReturnData() {
        // given
        VehicleDashboardSummaryResponse vehicles =
                org.mockito.Mockito.mock(VehicleDashboardSummaryResponse.class);

        TransactionDashboardSummaryResponse transactions =
                org.mockito.Mockito.mock(TransactionDashboardSummaryResponse.class);

        UserDashboardSummaryResponse users =
                org.mockito.Mockito.mock(UserDashboardSummaryResponse.class);

        StationDashboardSummaryResponse stations =
                org.mockito.Mockito.mock(StationDashboardSummaryResponse.class);

        when(vehicleClient.getVehicleSummary())
                .thenReturn(Optional.of(vehicles));

        when(transactionClient.getDashboardSummary())
                .thenReturn(Optional.of(transactions));

        when(userClient.getUserSummary())
                .thenReturn(Optional.of(users));

        when(stationClient.getStationSummary())
                .thenReturn(Optional.of(stations));

        // when
        DashboardAdminResponse result =
                dashboardService.getDashboard();

        // then
        assertThat(result).isNotNull();
        assertThat(result.vehicles()).isSameAs(vehicles);
        assertThat(result.transactions()).isSameAs(transactions);
        assertThat(result.users()).isSameAs(users);
        assertThat(result.stations()).isSameAs(stations);
    }

    @Test
    void getDashboard_shouldReturnNullSummaries_whenServicesReturnEmpty() {
        // given
        when(vehicleClient.getVehicleSummary())
                .thenReturn(Optional.empty());

        when(transactionClient.getDashboardSummary())
                .thenReturn(Optional.empty());

        when(userClient.getUserSummary())
                .thenReturn(Optional.empty());

        when(stationClient.getStationSummary())
                .thenReturn(Optional.empty());

        // when
        DashboardAdminResponse result =
                dashboardService.getDashboard();

        // then
        assertThat(result).isNotNull();
        assertThat(result.vehicles()).isNull();
        assertThat(result.transactions()).isNull();
        assertThat(result.users()).isNull();
        assertThat(result.stations()).isNull();
    }

    @Test
    void getDashboard_shouldReturnUserCounts_whenServicesReturnData() {
        // given
        UUID userId = UUID.randomUUID();

        VehicleCountResponse vehicles =
                new VehicleCountResponse(5L);

        TransactionCountResponse transactions =
                new TransactionCountResponse(12L);

        when(vehicleClient.getUserVehicleCount(userId))
                .thenReturn(Optional.of(vehicles));

        when(transactionClient.getUserTransactionsCount(userId))
                .thenReturn(Optional.of(transactions));

        // when
        DashboardUserResponse result =
                dashboardService.getDashboard(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.vehicleCount()).isEqualTo(5L);
        assertThat(result.transactionCount()).isEqualTo(12L);
    }

    @Test
    void getDashboard_shouldReturnZeroCounts_whenServicesReturnEmpty() {
        // given
        UUID userId = UUID.randomUUID();

        when(vehicleClient.getUserVehicleCount(userId))
                .thenReturn(Optional.empty());

        when(transactionClient.getUserTransactionsCount(userId))
                .thenReturn(Optional.empty());

        // when
        DashboardUserResponse result =
                dashboardService.getDashboard(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.vehicleCount()).isEqualTo(0L);
        assertThat(result.transactionCount()).isEqualTo(0L);
    }
}