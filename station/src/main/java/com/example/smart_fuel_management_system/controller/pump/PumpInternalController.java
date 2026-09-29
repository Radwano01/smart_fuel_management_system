package com.example.smart_fuel_management_system.controller.pump;


import com.example.smart_fuel_management_system.dto.station.StationFuelSessionResponse;
import com.example.smart_fuel_management_system.service.PumpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/internal/stations/pumps")
public class PumpInternalController {

    private final PumpService pumpService;

    @GetMapping("/{pumpId}")
    public ResponseEntity<StationFuelSessionResponse> getStationId(@PathVariable String pumpId){
        UUID id = UUID.fromString(pumpId);
        return ResponseEntity.ok(pumpService.getStationId(id));
    }
}
