package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.entity.Station;
import com.example.smart_fuel_management_system.entity.StationEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StationEmployeeRepository extends JpaRepository<StationEmployee, UUID> {
    boolean existsByStationId(UUID stationId);
    @Query("""
        SELECT se.station
        FROM StationEmployee se
        WHERE se.employeeId = :employeeId
        """)
    Optional<Station> findByEmployeeId(@Param("employeeId") UUID employeeId);
    Optional<StationEmployee> findByStationId(UUID stationId);
    Optional<StationEmployee> findStationEmployeeByEmployeeId(UUID employeeId);
}
