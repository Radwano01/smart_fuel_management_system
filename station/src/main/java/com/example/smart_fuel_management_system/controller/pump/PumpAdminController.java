package com.example.smart_fuel_management_system.controller.pump;

import com.example.smart_fuel_management_system.dto.pump.CreatePumpRequest;
import com.example.smart_fuel_management_system.dto.pump.PumpResponse;
import com.example.smart_fuel_management_system.service.PumpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/stations/{stationId}/pumps")
public class PumpAdminController {

    private final PumpService pumpService;

    @PostMapping
    public ResponseEntity<PumpResponse> create(@PathVariable("stationId") String id,
                                               @Valid @RequestBody CreatePumpRequest request) {
        UUID stationId = UUID.fromString(id);
        return ResponseEntity.status(201).body(pumpService.create(stationId, request));
    }
}