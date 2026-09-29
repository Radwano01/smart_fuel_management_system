package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.*;
import com.example.smart_fuel_management_system.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/internal/vehicles")
@RequiredArgsConstructor
public class VehicleInternalController {

    private final VehicleService vehicleService;

    @GetMapping("/resolve")
    public ResponseEntity<VehicleResponse> resolve(@RequestParam("rfidTag") String rfidTag,
                                                   @RequestParam("plateNumber") String plateNumber){
        return ResponseEntity.ok(vehicleService.resolve(rfidTag, plateNumber));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<VehicleCountResponse> getUserVehicleCount(@PathVariable UUID userId){
        return ResponseEntity.ok(
                vehicleService.getVehicleCount(userId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleTransactionResponse> getVehicleSummaryDetails(@PathVariable String id){
        UUID vehicleId = UUID.fromString(id);
        return ResponseEntity.ok(vehicleService.getVehicleSummaryDetails(vehicleId));
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<VehicleDashboardSummaryResponse> getDashboardSummary() {
        return ResponseEntity.ok(vehicleService.getDashboardSummary());
    }

    @PostMapping("/by-ids")
    public ResponseEntity<List<VehicleTransactionResponse>> getVehiclesByIds(
            @RequestBody List<UUID> ids) {

        return ResponseEntity.ok(
                vehicleService.getVehiclesByIds(ids)
        );
    }
}
