package com.example.smart_fuel_management_system.controller.stationEmployee;

import com.example.smart_fuel_management_system.dto.station.StationEmployeeAuthResponse;
import com.example.smart_fuel_management_system.service.StationEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/internal/station-employees")
public class StationEmployeeInternalController {

    private final StationEmployeeService stationEmployeeService;

    @GetMapping("/{employeeId}")
    public ResponseEntity<StationEmployeeAuthResponse> getStationDetails(@PathVariable String employeeId){
        UUID id = UUID.fromString(employeeId);
        return ResponseEntity.ok(stationEmployeeService.getStationDetails(id));
    }
}
