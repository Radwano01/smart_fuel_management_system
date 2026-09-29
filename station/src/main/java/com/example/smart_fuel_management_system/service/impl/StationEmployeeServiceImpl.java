package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.station.SaveStationUser;
import com.example.smart_fuel_management_system.dto.station.StationEmployeeAuthResponse;
import com.example.smart_fuel_management_system.entity.Station;
import com.example.smart_fuel_management_system.entity.StationEmployee;
import com.example.smart_fuel_management_system.repository.StationEmployeeRepository;
import com.example.smart_fuel_management_system.service.StationEmployeeService;
import com.example.smart_fuel_management_system.service.StationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class StationEmployeeServiceImpl implements StationEmployeeService {

    private final StationEmployeeRepository stationEmployeeRepository;
    private final StationService stationService;
    private final ObjectMapper objectMapper;

    @Override
    public void create(String payload) throws JsonProcessingException {
        SaveStationUser createdEmployee = objectMapper.readValue(
                payload,
                SaveStationUser.class
        );

        StationEmployee employee = new StationEmployee(
                createdEmployee.employeeId(),
                null
        );

        stationEmployeeRepository.save(employee);
    }

    @Transactional(readOnly = true)
    @Override
    public Station findStationByEmployeeId(UUID employeeId) {
        return stationEmployeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(()-> new EntityNotFoundException("Employee does not exist"));
    }

    @Transactional(readOnly = true)
    @Override
    public StationEmployeeAuthResponse getStationDetails(UUID employeeId) {
        Station station = findStationByEmployeeId(employeeId);

        return StationEmployeeAuthResponse.builder()
                .id(station.getId())
                .name(station.getName())
                .city(station.getCity())
                .address(station.getAddress())
                .build();
    }

    @Transactional
    @Override
    public void changeStation(
            UUID employeeId,
            UUID stationId) {

        StationEmployee employee =
                stationEmployeeRepository.findStationEmployeeByEmployeeId(employeeId)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Station employee not found: "
                                                + employeeId
                                ));

        if (employee.getStation() != null &&
                employee.getStation().getId().equals(stationId)) {

            throw new EntityExistsException(
                    "Employee is already assigned to this station"
            );
        }

        stationEmployeeRepository.findByStationId(stationId).ifPresent(relation -> relation.setStation(null));

        Station station = stationService.getStation(stationId);

        employee.setStation(station);
    }

    @Transactional
    @Override
    public void removeStation(UUID stationId) {
        StationEmployee station = stationEmployeeRepository.findByStationId(stationId)
                .orElseThrow(()-> new EntityNotFoundException("station not found"));

        station.setStation(null);
    }
}
