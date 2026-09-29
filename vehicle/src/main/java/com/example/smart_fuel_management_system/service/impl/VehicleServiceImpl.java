package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.*;
import com.example.smart_fuel_management_system.entity.Vehicle;
import com.example.smart_fuel_management_system.enums.VehicleStatusType;
import com.example.smart_fuel_management_system.repository.VehicleRepository;
import com.example.smart_fuel_management_system.service.VehicleService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import com.example.smart_fuel_management_system.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    private final VehicleSpecService vehicleSpecService;

    @Override
    public void create(UUID userId, AddRequest request) {

        boolean exists = vehicleRepository.existsByPlateNumber(request.plateNumber());

        if(exists){
            throw new EntityExistsException("plate number is already exist");
        }

        BigDecimal tankCapacity = vehicleSpecService.getTankCapacity(
                request.brand(),
                request.model(),
                request.year(),
                request.fuelType()
        );

        Vehicle vehicle = new Vehicle(
                request.plateNumber(),
                request.brand(),
                request.model(),
                request.year(),
                tankCapacity,
                request.fuelType(),
                null,
                VehicleStatusType.PENDING,
                userId
        );

        vehicleRepository.save(vehicle);
    }


    @Override
    public void update(UUID userId, UUID vehicleId, UpdateRequest request) {
        Vehicle vehicle = vehicleRepository.findByUserIdAndId(userId, vehicleId)
                .orElseThrow(()-> new EntityNotFoundException("This user does not own this vehicle or vehicle is not exist"));

        if(hasText(request.brand())){
            vehicle.setBrand(request.brand());
        }

        if(hasText(request.model())){
            vehicle.setModel(request.model());
        }

        if(request.fuelType() != null){
            vehicle.setFuelType(request.fuelType());
        }

        vehicleRepository.save(vehicle);
    }

    @Override
    public void deactivate(UUID userId, UUID vehicleId) {

        Vehicle vehicle = vehicleRepository.findByUserIdAndId(userId, vehicleId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "This user does not own this vehicle or vehicle does not exist"
                ));

        if(vehicle.getStatus() == VehicleStatusType.PENDING){
            throw new BadRequestException("This Vehicle status cannot move from PENDING stage to INACTIVE!");
        }

        vehicle.setStatus(VehicleStatusType.INACTIVE);

        vehicleRepository.save(vehicle);
    }

    @Override
    public void activate(UUID userId, UUID vehicleId) {
        Vehicle vehicle = vehicleRepository.findByUserIdAndId(userId, vehicleId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "This user does not own this vehicle or vehicle does not exist"
                ));

        if(vehicle.getStatus() == VehicleStatusType.PENDING){
            throw new BadRequestException("This Vehicle status cannot move from PENDING stage to ACTIVE!");
        }

        vehicle.setStatus(VehicleStatusType.ACTIVE);

        vehicleRepository.save(vehicle);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ListResponse> findAllByUserId(UUID userId) {
        return vehicleRepository.findAllByUserId(userId)
                .stream().map(v -> new ListResponse(
                            v.getId(),
                            v.getBrand(),
                            v.getModel(),
                            v.getYear(),
                            v.getStatus()
                    )
                ).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public VehicleDTO findByUserIdAndVehicleId(UUID userId, UUID vehicleId) {

        Vehicle vehicle = vehicleRepository.findByUserIdAndId(userId, vehicleId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "This vehicle is not found"
                ));

        return toVehicleDto(vehicle);
    }

    @Transactional(readOnly = true)
    @Override
    public VehicleResponse resolve(String rfidTag, String plateNumber) {
        Vehicle vehicle = vehicleRepository.findByRfidTagAndPlateNumber(rfidTag, plateNumber)
                .orElseThrow(()-> new EntityNotFoundException("Vehicle Not found"));

        return VehicleResponse.builder()
                .userId(vehicle.getUserId())
                .vehicleId(vehicle.getId())
                .fuelType(vehicle.getFuelType())
                .tankCapacity(vehicle.getTankCapacity())
                .vehicleStatusType(vehicle.getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public VehicleCountResponse getVehicleCount(UUID userId){
        return new VehicleCountResponse(vehicleRepository.countByUserId(userId));
    }

    @Transactional(readOnly = true)
    @Override
    public VehicleTransactionResponse getVehicleSummaryDetails(UUID id) {
            return vehicleRepository.findVehicleForTransaction(id)
                    .orElseThrow(()->new EntityNotFoundException("This car is not found in the db"));
    }

    @Transactional(readOnly = true)
    @Override
    public VehicleDashboardSummaryResponse getDashboardSummary() {

        List<RecentVehicleResponse> recentVehicles =
                vehicleRepository.findTop10ByOrderByCreatedAtDesc()
                        .stream()
                        .map(vehicle -> new RecentVehicleResponse(
                                vehicle.getId(),
                                vehicle.getPlateNumber(),
                                vehicle.getBrand(),
                                vehicle.getModel(),
                                vehicle.getCreatedAt()
                        ))
                        .toList();

        return new VehicleDashboardSummaryResponse(
                vehicleRepository.count(),
                vehicleRepository.countByStatus(VehicleStatusType.ACTIVE),
                recentVehicles
        );
    }


    @Transactional(readOnly = true)
    @Override
    public List<VehicleTransactionResponse> getVehiclesByIds(List<UUID> ids) {

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return vehicleRepository.findVehiclesByIds(ids);
    }

    @Override
    public VehicleStationResponse getVehicleByPlateNumber(String plateNumber) {
        Vehicle vehicle = vehicleRepository.findByPlateNumber(plateNumber)
                .orElseThrow(()-> new EntityNotFoundException("vehicle not found"));

        return new VehicleStationResponse(
                vehicle.getId(),
                vehicle.getPlateNumber(),
                vehicle.getRfidTag(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getStatus()
        );
    }

    private VehicleDTO toVehicleDto(Vehicle vehicle) {
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
