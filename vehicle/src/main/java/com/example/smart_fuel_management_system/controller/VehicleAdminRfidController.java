package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.AssignRfidRequest;
import com.example.smart_fuel_management_system.dto.VehicleDTO;
import com.example.smart_fuel_management_system.service.VehicleRfidService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/vehicles")
@RequiredArgsConstructor
public class VehicleAdminRfidController {

    private final VehicleRfidService vehicleRfidService;

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

    @GetMapping("/rfid/{rfid}")
    public ResponseEntity<VehicleDTO> findByRfid(
            @PathVariable String rfid) {

        return ResponseEntity.ok(
                vehicleRfidService.findByRfid(rfid)
        );
    }

    @DeleteMapping("/{vehicleId}/rfid")
    public ResponseEntity<Void> removeRfid(
            @PathVariable UUID vehicleId) {

        vehicleRfidService.removeRfid(vehicleId);

        return ResponseEntity.noContent().build();
    }
}