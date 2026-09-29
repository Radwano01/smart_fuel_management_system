package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.entity.FuelSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FuelSessionRepository extends JpaRepository<FuelSession, UUID> {
}
