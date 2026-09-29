package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.entity.VehicleSpec;
import com.example.smart_fuel_management_system.enums.FuelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleSpecRepository extends JpaRepository<VehicleSpec, Long> {
    Optional<VehicleSpec> findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType(String brand, String model, int year, FuelType fuelType);
}
