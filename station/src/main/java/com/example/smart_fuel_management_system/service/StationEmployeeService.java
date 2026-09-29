package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.station.StationEmployeeAuthResponse;
import com.example.smart_fuel_management_system.entity.Station;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.UUID;

public interface StationEmployeeService {
    void create(String payload) throws JsonProcessingException;
    Station findStationByEmployeeId(UUID employeeId);
    StationEmployeeAuthResponse getStationDetails(UUID employeeId);
    void changeStation(UUID employeeId, UUID stationId);
    void removeStation(UUID stationId);
}
