package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.pump.CreatePumpRequest;
import com.example.smart_fuel_management_system.dto.pump.PumpResponse;
import com.example.smart_fuel_management_system.dto.station.StationFuelSessionResponse;
import com.example.smart_fuel_management_system.entity.Pump;
import com.example.smart_fuel_management_system.entity.Station;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.repository.PumpRepository;
import com.example.smart_fuel_management_system.service.PumpHeartbeatService;
import com.example.smart_fuel_management_system.service.PumpService;
import com.example.smart_fuel_management_system.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PumpServiceImpl implements PumpService {

    private final PumpRepository pumpRepository;
    private final StationService stationService;
    private final PumpHeartbeatService pumpHeartbeatService;

    @Override
    public PumpResponse create(UUID stationId, CreatePumpRequest request) {
        if (request.fuelTypes().contains(FuelType.ELECTRIC) && request.fuelTypes().size() > 1) {
            throw new IllegalArgumentException(
                    "ELECTRIC fuel type cannot be combined with other fuel types"
            );
        }

        Station station = stationService.getStation(stationId);

        long count = pumpRepository.findMaxPumpNumberByStationId(stationId);

        Pump pump = new Pump(
                count + 1,
                new HashSet<>(request.fuelTypes()),
                station
        );

        Pump savedPump = pumpRepository.save(pump);

        return pumpHeartbeatService.enrich(savedPump);
    }

    @Transactional(readOnly = true)
    @Override
    public StationFuelSessionResponse getStationId(UUID id) {
        return pumpRepository.findStationIdAndPumpIdById(id);
    }
}