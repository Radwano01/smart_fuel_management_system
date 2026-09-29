package com.example.smart_fuel_management_system.controller.stationEmployee;


import com.example.smart_fuel_management_system.service.StationEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/stations/{stationId}/employees")
public class StationEmployeeAdminController {

    private final StationEmployeeService stationEmployeeService;

    @PatchMapping("/{employeeId}")
    public ResponseEntity<Void> changeStation(
            @PathVariable String employeeId,
            @PathVariable String stationId) {

        UUID empId = UUID.fromString(employeeId);
        UUID staId = UUID.fromString(stationId);
        stationEmployeeService.changeStation(
                empId,
                staId
        );

        return ResponseEntity.noContent().build();
    }

    @PatchMapping
    public ResponseEntity<Void> removeStation(
            @PathVariable String stationId) {
        UUID id = UUID.fromString(stationId);
        stationEmployeeService.removeStation(
                id
        );

        return ResponseEntity.noContent().build();
    }
}
