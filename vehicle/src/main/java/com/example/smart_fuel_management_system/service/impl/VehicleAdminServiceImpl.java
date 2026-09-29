package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.ListResponse;
import com.example.smart_fuel_management_system.dto.VehicleDTO;
import com.example.smart_fuel_management_system.dto.VehicleTransactionResponse;
import com.example.smart_fuel_management_system.entity.Vehicle;
import com.example.smart_fuel_management_system.enums.VehicleStatusType;
import com.example.smart_fuel_management_system.repository.VehicleRepository;
import com.example.smart_fuel_management_system.service.VehicleAdminService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleAdminServiceImpl implements VehicleAdminService {

    private final VehicleRepository vehicleRepository;

    @Override
    public void changeStatus(UUID vehicleId, VehicleStatusType status) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("This vehicle is not found"));

        vehicle.setStatus(status);
        vehicleRepository.save(vehicle);
    }

    @Override
    public void delete(UUID vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new EntityNotFoundException("This vehicle is not found");
        }

        vehicleRepository.deleteById(vehicleId);
    }

    @Transactional(readOnly = true)
    @Override
    public VehicleDTO findByPlateNumber(String plateNumber) {
        Vehicle vehicle = vehicleRepository.findByPlateNumber(plateNumber)
                .orElseThrow(() -> new EntityNotFoundException("This vehicle is not found"));

        return VehicleDTO.builder()
                .id(vehicle.getId())
                .plateNumber(vehicle.getPlateNumber())
            .rfidTag(vehicle.getRfidTag())
                .year(vehicle.getYear())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .fuelType(vehicle.getFuelType())
                .status(vehicle.getStatus())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public List<VehicleDTO> getUserVehicles(UUID userId) {

        return vehicleRepository.findByUserId(userId)
                .stream()
                .map(vehicle -> new VehicleDTO(
                        vehicle.getId(),
                        vehicle.getPlateNumber(),
                        vehicle.getRfidTag(),
                        vehicle.getBrand(),
                        vehicle.getModel(),
                        vehicle.getYear(),
                        vehicle.getFuelType(),
                        vehicle.getStatus(),
                        vehicle.getCreatedAt(),
                        vehicle.getUpdatedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ListResponse> getVehicles(Pageable pageable) {

        return vehicleRepository
                .findAll(pageable)
                .map(vehicle -> ListResponse.builder()
                        .id(vehicle.getId())
                        .brand(vehicle.getBrand())
                        .model(vehicle.getModel())
                        .year(vehicle.getYear())
                        .status(vehicle.getStatus())
                        .build());
    }

    @Transactional(readOnly = true)
    @Override
    public VehicleDTO getVehicleDetails(UUID vehicleId) {

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Vehicle not found: " + vehicleId
                        ));

        return VehicleDTO.builder()
                .id(vehicle.getId())
                .plateNumber(vehicle.getPlateNumber())
                .rfidTag(vehicle.getRfidTag())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .fuelType(vehicle.getFuelType())
                .status(vehicle.getStatus())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}