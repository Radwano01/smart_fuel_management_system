package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.entity.PumpDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PumpDeviceRepository extends JpaRepository<PumpDevice, UUID> {
    Optional<PumpDevice> findByDeviceId(String deviceId);
    Optional<PumpDevice> findByPump_Id(UUID pumpId);
    boolean existsByPump_Id(UUID pumpId);
}