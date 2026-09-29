package com.example.smart_fuel_management_system.controller;


import com.example.smart_fuel_management_system.dto.ListResponse;
import com.example.smart_fuel_management_system.dto.VehicleDTO;
import com.example.smart_fuel_management_system.dto.VehicleStatusRequest;
import com.example.smart_fuel_management_system.service.VehicleAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/vehicles")
public class VehicleAdminController {

    private final VehicleAdminService adminVehicleService;

    @PatchMapping("/{vehicleId}/status")
    public ResponseEntity<Void> changeStatus(
            @PathVariable UUID vehicleId,
            @Valid @RequestBody VehicleStatusRequest request) {

        adminVehicleService.changeStatus(vehicleId, request.status());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<Void> delete(@PathVariable UUID vehicleId) {
        adminVehicleService.delete(vehicleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/plates/{plateNumber}")
    public ResponseEntity<VehicleDTO> findByPlateNumber(@PathVariable String plateNumber) {
        return ResponseEntity.ok(adminVehicleService.findByPlateNumber(plateNumber));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<VehicleDTO>> getUserVehicles(@PathVariable String userId) {
        UUID id = UUID.fromString(userId);
        return ResponseEntity.ok(adminVehicleService.getUserVehicles(id));
    }

    @GetMapping
    public ResponseEntity<Page<ListResponse>> getVehicles(
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                adminVehicleService.getVehicles(pageable)
        );
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<VehicleDTO> getVehicleDetails(
            @PathVariable UUID vehicleId) {

        return ResponseEntity.ok(
                adminVehicleService.getVehicleDetails(vehicleId)
        );
    }
}