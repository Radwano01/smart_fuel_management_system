package com.example.smart_fuel_management_system.controller;


import com.example.smart_fuel_management_system.dto.AssignRfidRequest;
import com.example.smart_fuel_management_system.dto.VehicleStationResponse;
import com.example.smart_fuel_management_system.service.VehicleRfidService;
import com.example.smart_fuel_management_system.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/station/vehicles")
@RequiredArgsConstructor
public class VehicleStationRfidController {

    private final VehicleRfidService vehicleRfidService;
    private final VehicleService vehicleService;

    @PostMapping("/{vehicleId}/rfid")
    public ResponseEntity<Void> assignRfid(
            @PathVariable UUID vehicleId,
            @RequestBody AssignRfidRequest request) {

        vehicleRfidService.assignRfid(
                vehicleId,
                request.rfid()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rfid/vehicle/{plateNumber}")
    public ResponseEntity<VehicleStationResponse> getVehicleForStation(@PathVariable String plateNumber){
        return ResponseEntity.ok(
                vehicleService.getVehicleByPlateNumber(plateNumber)
        );
    }

    @DeleteMapping("/{vehicleId}/rfid")
    public ResponseEntity<Void> removeRfid(@PathVariable UUID vehicleId) {

        vehicleRfidService.removeRfid(vehicleId);

        return ResponseEntity.noContent().build();
    }

}
