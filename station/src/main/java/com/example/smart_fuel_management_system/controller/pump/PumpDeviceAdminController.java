package com.example.smart_fuel_management_system.controller.pump;

import com.example.smart_fuel_management_system.dto.pump.AssignPumpDeviceRequest;
import com.example.smart_fuel_management_system.dto.pump.PumpDeviceResponse;
import com.example.smart_fuel_management_system.service.PumpDeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/pump-devices")
@RequiredArgsConstructor
public class PumpDeviceAdminController {

    private final PumpDeviceService pumpDeviceService;


    @GetMapping("/{deviceId}")
    public ResponseEntity<PumpDeviceResponse> getByDeviceId(
            @PathVariable String deviceId
    ) {
        return ResponseEntity.ok(
                pumpDeviceService.getByDeviceId(deviceId)
        );
    }

    @PutMapping("/{pumpId}/device")
    public ResponseEntity<PumpDeviceResponse> assignDevice(
            @PathVariable String pumpId,
            @Valid @RequestBody AssignPumpDeviceRequest request
    ) {
        UUID id = UUID.fromString(pumpId);

        return ResponseEntity.ok(
                pumpDeviceService.assignToPump(
                        id,
                        request.deviceId()
                )
        );
    }

    @DeleteMapping("/{pumpId}/device")
    public ResponseEntity<Void> unassignDevice(
            @PathVariable String pumpId
    ) {
        UUID id = UUID.fromString(pumpId);

        pumpDeviceService.unassignFromPump(id);

        return ResponseEntity.noContent().build();
    }
}