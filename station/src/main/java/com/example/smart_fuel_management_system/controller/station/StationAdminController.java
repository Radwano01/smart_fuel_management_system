package com.example.smart_fuel_management_system.controller.station;

import com.example.smart_fuel_management_system.dto.station.StationDetailsResponse;
import com.example.smart_fuel_management_system.dto.station.StationSummaryResponse;
import com.example.smart_fuel_management_system.dto.station.UpdateStationRequest;
import com.example.smart_fuel_management_system.enums.StationStatusType;
import com.example.smart_fuel_management_system.service.StationService;
import com.example.smart_fuel_management_system.dto.station.CreateStationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/stations")
public class StationAdminController {

    private final StationService stationService;

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateStationRequest request) {
        stationService.create(request);
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/{stationId}")
    public ResponseEntity<StationDetailsResponse> getStationDetails(
            @PathVariable String stationId
    ) {
        UUID id = UUID.fromString(stationId);
        return ResponseEntity.ok(
                stationService.getStationDetails(id)
        );
    }

    @PatchMapping("/{stationId}")
    public ResponseEntity<Void> updateStation(
            @PathVariable String stationId,
            @RequestBody UpdateStationRequest request
    ) {
        UUID id = UUID.fromString(stationId);
        stationService.updateStation(id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<StationSummaryResponse>> getStations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) StationStatusType status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return ResponseEntity.ok(
                stationService.searchAndFilterStations(
                        search,
                        status,
                        pageable
                )
        );
    }
}
