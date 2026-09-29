package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface VehicleService {
    void create(UUID userId, AddRequest request);
    void update(UUID userId, UUID vehicleId, UpdateRequest request);
    void deactivate(UUID userId, UUID vehicleId);
    void activate(UUID userId, UUID vehicleId);
    List<ListResponse> findAllByUserId(UUID userId);
    VehicleDTO findByUserIdAndVehicleId(UUID userId, UUID vehicleId);
    VehicleResponse resolve(String rfidTag, String plateNumber);
    VehicleCountResponse getVehicleCount(UUID userId);
    VehicleTransactionResponse getVehicleSummaryDetails(UUID id);
    VehicleDashboardSummaryResponse getDashboardSummary();
    List<VehicleTransactionResponse> getVehiclesByIds(List<UUID> ids);
    VehicleStationResponse getVehicleByPlateNumber(String plateNumber);
}
