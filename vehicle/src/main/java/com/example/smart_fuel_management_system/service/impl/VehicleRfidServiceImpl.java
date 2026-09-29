package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.VehicleDTO;
import com.example.smart_fuel_management_system.entity.Vehicle;
import com.example.smart_fuel_management_system.exception.BadRequestException;
import com.example.smart_fuel_management_system.repository.VehicleRepository;
import com.example.smart_fuel_management_system.service.VehicleRfidService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleRfidServiceImpl implements VehicleRfidService {

    private final VehicleRepository vehicleRepository;

    @Override
    public void assignRfid(UUID vehicleId, String rfid) {

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Vehicle not found"));

        assignRfid(vehicle, rfid);
    }

    private void assignRfid(Vehicle vehicle, String rfid) {

        if (rfid == null || rfid.isBlank()) {
            throw new BadRequestException("RFID cannot be empty");
        }

        if (vehicle.getRfidTag() != null) {
            if (vehicle.getRfidTag().equals(rfid)) {
                throw new BadRequestException("RFID is already assigned to this vehicle");
            }

            throw new BadRequestException("Vehicle already has an RFID assigned; remove it before assigning another");
        }

        vehicleRepository.findByRfidTag(rfid).ifPresent(existingVehicle -> {
            throw new EntityExistsException("RFID already assigned to another vehicle");
        });

        vehicle.setRfidTag(rfid);

        vehicleRepository.save(vehicle);
    }

    @Transactional(readOnly = true)
    @Override
    public VehicleDTO findByRfid(String rfid) {
        if (rfid == null || rfid.isBlank()) {
            throw new BadRequestException("RFID cannot be empty");
        }

        Vehicle vehicle =  vehicleRepository.findByRfidTag(rfid)
                .orElseThrow(() ->
                        new EntityNotFoundException("Vehicle not found with RFID: " + rfid));

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

    @Transactional
    @Override
    public void removeRfid(UUID vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Vehicle not found"));

        if (vehicle.getRfidTag() == null) {
            throw new BadRequestException("Vehicle has no RFID assigned");
        }

        vehicle.setRfidTag(null);
        vehicleRepository.save(vehicle);
    }
}
