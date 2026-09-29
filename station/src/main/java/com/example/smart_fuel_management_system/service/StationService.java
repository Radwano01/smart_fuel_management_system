package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.station.*;
import com.example.smart_fuel_management_system.entity.Station;
import com.example.smart_fuel_management_system.enums.StationStatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface StationService {
    void create(CreateStationRequest request);
    void changeStatus(StationStatusType stationStatusType, UUID employeeId);
    StationTransactionResponse getDetailsForTransaction(UUID stationId);
    StationResponse getDetailsForStation(UUID employeeId);
    StationValidationResponse validateStation(UUID stationId);
    Station getStation(UUID stationId);
    StationDashboardSummaryResponse getDashboardSummary();
    void updateStation(UUID stationId, UpdateStationRequest request);
    Page<StationSummaryResponse> searchAndFilterStations(
            String search,
            StationStatusType status,
            Pageable pageable
    );
    StationDetailsResponse getStationDetails(UUID stationId);
    List<StationTransactionResponse> getStationsByIds(List<UUID> ids);
}