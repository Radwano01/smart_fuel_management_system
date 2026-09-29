package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.repository.MissingVehicleSpecRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MissingVehicleSpecService {

    private final MissingVehicleSpecRepository missingVehicleSpecRepository;

    void save(String brand,
              String model,
              int year,
              FuelType fuelType){
        missingVehicleSpecRepository.saveIfNotExists(
                brand,
                model,
                year,
                fuelType.name()
        );
    }

}
