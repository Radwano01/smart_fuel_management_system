package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.entity.VehicleSpec;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.repository.VehicleSpecRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class VehicleSpecService {

    private final VehicleSpecRepository vehicleSpecRepository;

    private final MissingVehicleSpecService missingVehicleSpecService;

    private static final BigDecimal DEFAULT_TANK_CAPACITY =
            BigDecimal.valueOf(50);

    @Transactional
    public BigDecimal getTankCapacity(String brand,
                                      String model,
                                      int year,
                                      FuelType fuelType) {
        return vehicleSpecRepository
                .findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType(
                        brand,
                        model,
                        year,
                        fuelType
                ).map(VehicleSpec::getTankCapacity)
                .orElseGet(() -> {
                    try {
                        missingVehicleSpecService.save(
                                brand,
                                model,
                                year,
                                fuelType
                        );
                    } catch (DataIntegrityViolationException ignored) {
                    }
                    return DEFAULT_TANK_CAPACITY;
                });
    }
}