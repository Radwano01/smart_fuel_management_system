package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.RegisterRequest;
import com.example.smart_fuel_management_system.dto.RegisterStationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface RegisterService {
    void register(RegisterRequest request) throws JsonProcessingException;
    void registerStationEmployee(RegisterStationRequest request) throws JsonProcessingException;
}
