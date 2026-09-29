package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.*;
import com.example.smart_fuel_management_system.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<Void> create(
            Authentication authentication,
            @Valid @RequestBody AddRequest request) {

        UUID userId = UUID.fromString(authentication.getName());

        vehicleService.create(userId, request);
        return ResponseEntity.status(201).build();
    }

    @GetMapping
    public ResponseEntity<List<ListResponse>> list(Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                vehicleService.findAllByUserId(userId)
        );
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<VehicleDTO> getById(
            Authentication authentication,
            @PathVariable UUID vehicleId) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                vehicleService.findByUserIdAndVehicleId(userId, vehicleId)
        );
    }


    @PatchMapping("/{vehicleId}")
    public ResponseEntity<Void> update(
            Authentication authentication,
            @PathVariable UUID vehicleId,
            @RequestBody UpdateRequest request) {

        UUID userId = UUID.fromString(authentication.getName());

        vehicleService.update(userId, vehicleId, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{vehicleId}/deactivate")
    public ResponseEntity<Void> deactivate(
            Authentication authentication,
            @PathVariable UUID vehicleId) {

        UUID userId = UUID.fromString(authentication.getName());

        vehicleService.deactivate(userId, vehicleId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{vehicleId}/activate")
    public ResponseEntity<Void> activate(
            Authentication authentication,
            @PathVariable UUID vehicleId) {

        UUID userId = UUID.fromString(authentication.getName());

        vehicleService.activate(userId, vehicleId);
        return ResponseEntity.noContent().build();
    }
}