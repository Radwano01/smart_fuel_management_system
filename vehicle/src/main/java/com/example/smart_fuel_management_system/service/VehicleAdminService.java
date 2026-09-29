package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.ListResponse;
import com.example.smart_fuel_management_system.dto.VehicleDTO;
import com.example.smart_fuel_management_system.dto.VehicleTransactionResponse;
import com.example.smart_fuel_management_system.enums.VehicleStatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface VehicleAdminService {
    void changeStatus(UUID vehicleId, VehicleStatusType status);
    void delete(UUID vehicleId);
    VehicleDTO findByPlateNumber(String plateNumber);
    List<VehicleDTO> getUserVehicles(UUID id);
    Page<ListResponse> getVehicles(Pageable pageable);
    VehicleDTO getVehicleDetails(UUID vehicleId);
}