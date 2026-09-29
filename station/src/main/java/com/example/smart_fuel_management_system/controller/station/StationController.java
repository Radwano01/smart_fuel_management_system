package com.example.smart_fuel_management_system.controller.station;

import com.example.smart_fuel_management_system.dto.station.StationResponse;
import com.example.smart_fuel_management_system.enums.StationStatusType;
import com.example.smart_fuel_management_system.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

//STATION + ADMIN ONLY HAS CONTROL ON THIS CONTROLLER
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/stations")
public class StationController {

    private final StationService stationService;

    @GetMapping
    public ResponseEntity<StationResponse> getDetailsForStation(Principal principal){
        UUID employeeId = UUID.fromString(principal.getName());
        return ResponseEntity.ok(stationService.getDetailsForStation(employeeId));
    }


    @PatchMapping("/status")
    public ResponseEntity<Void> changeStatus(@RequestParam StationStatusType status, Principal principal) {
        UUID employeeId = UUID.fromString(principal.getName());
        stationService.changeStatus(status, employeeId);
        return ResponseEntity.noContent().build();
    }
}