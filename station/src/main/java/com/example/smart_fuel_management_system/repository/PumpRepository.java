package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.dto.station.StationFuelSessionResponse;
import com.example.smart_fuel_management_system.entity.Pump;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PumpRepository extends JpaRepository<Pump, UUID> {
    @Query("""
    SELECT COALESCE(MAX(p.pumpNumber), 0)
    FROM Pump p
    WHERE p.station.id = :stationId
    """)
    long findMaxPumpNumberByStationId(UUID stationId);

    @Query("SELECT new com.example.smart_fuel_management_system.dto.station.StationFuelSessionResponse(p.station.id, p.id) FROM Pump p WHERE p.id = :id")
    StationFuelSessionResponse findStationIdAndPumpIdById(@Param("id") UUID id);
}