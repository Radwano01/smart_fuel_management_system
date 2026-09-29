package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.dto.fuelPrice.FuelPriceResponse;
import com.example.smart_fuel_management_system.entity.FuelPrice;
import com.example.smart_fuel_management_system.enums.FuelPriceStatusType;
import com.example.smart_fuel_management_system.enums.FuelType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FuelPriceRepository extends JpaRepository<FuelPrice, UUID> {

    Optional<FuelPrice> findByStationIdAndFuelTypeAndFuelPriceStatusType(
            UUID stationId,
            FuelType fuelType,
            FuelPriceStatusType status
    );

    @Query("""
    SELECT new com.example.smart_fuel_management_system.dto.FuelPriceResponse(
        fp.id,
        fp.fuelType,
        fp.price,
        fp.createdAt
    )
    FROM FuelPrice fp
    WHERE fp.station.id = :stationId AND fp.fuelPriceStatusType = com.example.smart_fuel_management_system.enums.FuelPriceStatusType.INACTIVE
    ORDER BY fp.createdAt DESC
    """)
    List<FuelPriceResponse> findStationPriceHistory(UUID stationId);

    @Query("""
    SELECT new com.example.smart_fuel_management_system.dto.FuelPriceResponse(
        fp.id,
        fp.fuelType,
        fp.price,
        fp.createdAt
    )
    FROM FuelPrice fp
    WHERE fp.station.id = :stationId
      AND (:fuelType IS NULL OR fp.fuelType = :fuelType)
      AND (:status IS NULL OR fp.fuelPriceStatusType = :status)
    ORDER BY fp.createdAt DESC
    """)
    Page<FuelPriceResponse> searchStationPrices(
            @Param("stationId") UUID stationId,
            @Param("fuelType") FuelType fuelType,
            @Param("status") FuelPriceStatusType status,
            Pageable pageable
    );

    List<FuelPrice> findByStationIdAndFuelPriceStatusType(UUID stationId, FuelPriceStatusType fuelPriceStatusType);
}
