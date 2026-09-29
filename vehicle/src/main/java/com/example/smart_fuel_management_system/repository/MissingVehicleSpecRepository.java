package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.entity.MissingVehicleSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MissingVehicleSpecRepository extends JpaRepository<MissingVehicleSpec, Long> {
    @Modifying
    @Query(value = """
        INSERT INTO missing_vehicle_specs
            (brand, model, spec_year, fuel_type)
        VALUES
            (:brand, :model, :year, :fuelType)
        ON CONFLICT (brand, model, spec_year, fuel_type)
        DO NOTHING
        """, nativeQuery = true)
    void saveIfNotExists(
            @Param("brand") String brand,
            @Param("model") String model,
            @Param("year") int year,
            @Param("fuelType") String fuelType
    );

}
