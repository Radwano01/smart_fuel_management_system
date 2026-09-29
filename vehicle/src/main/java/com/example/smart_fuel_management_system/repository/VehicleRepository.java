package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.dto.VehicleTransactionResponse;
import com.example.smart_fuel_management_system.entity.Vehicle;
import com.example.smart_fuel_management_system.enums.VehicleStatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    boolean existsByPlateNumber(String plateNumber);

    List<Vehicle> findAllByUserId(UUID userId);

    Optional<Vehicle> findByUserIdAndId(UUID userId, UUID vehicleId);

    Optional<Vehicle> findByPlateNumber(String plateNumber);

    Optional<Vehicle> findByRfidTag(String rfid);

    Optional<Vehicle> findByRfidTagAndPlateNumber(String rfidTag, String plateNumber);

    long countByUserId(UUID userId);

    @Query("""
            SELECT new com.example.smart_fuel_management_system.dto.VehicleTransactionResponse(
                v.id,
                v.plateNumber,
                v.brand,
                v.model,
                v.year
            )
            FROM Vehicle v
            WHERE v.id = :id
            """)
    Optional<VehicleTransactionResponse> findVehicleForTransaction(UUID id);

    long countByStatus(VehicleStatusType vehicleStatusType);

    List<Vehicle> findTop10ByOrderByCreatedAtDesc();

    @Query("""
        SELECT new com.example.smart_fuel_management_system.dto.VehicleTransactionResponse(
            v.id,
            v.plateNumber,
            v.brand,
            v.model,
            v.year
        )
        FROM Vehicle v
        WHERE v.id IN :ids
        """)
    List<VehicleTransactionResponse> findVehiclesByIds(
            @Param("ids") List<UUID> ids
    );

    List<Vehicle> findByUserId(UUID userId);

}