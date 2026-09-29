package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.VehicleDTO;
import com.example.smart_fuel_management_system.entity.Vehicle;

import java.util.UUID;

    public interface VehicleRfidService {
        void assignRfid(UUID vehicleId, String rfid);
        VehicleDTO findByRfid(String rfid);
        void removeRfid(UUID vehicleId);
    }
