package com.example.smart_fuel_management_system.controller.station;

import com.example.smart_fuel_management_system.dto.station.StationDashboardSummaryResponse;
import com.example.smart_fuel_management_system.dto.station.StationTransactionResponse;
import com.example.smart_fuel_management_system.dto.station.StationValidationResponse;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.service.FuelPriceService;
import com.example.smart_fuel_management_system.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/internal/stations")
public class StationInternalController {

    private final FuelPriceService fuelPriceService;
    private final StationService stationService;


    @GetMapping("/{id}/fuel-prices/{fuelType}")
    public ResponseEntity<BigDecimal> getFuelPrice(
            @PathVariable String id,
            @PathVariable FuelType fuelType
    ) {
        UUID stationId = UUID.fromString(id);
        return ResponseEntity.ok(
                fuelPriceService.getPrice(stationId, fuelType)
        );
    }

    @PostMapping("/by-ids")
    public ResponseEntity<List<StationTransactionResponse>> getStationsByIds(
            @RequestBody List<UUID> ids) {

        return ResponseEntity.ok(
                stationService.getStationsByIds(ids)
        );
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<StationTransactionResponse> getDetailsForTransaction(@PathVariable String id){
        UUID stationId = UUID.fromString(id);
        return ResponseEntity.ok(stationService.getDetailsForTransaction(stationId));
    }

    @GetMapping("/{id}/validate")
    public ResponseEntity<StationValidationResponse> validateStation(@PathVariable String id){
        UUID stationId = UUID.fromString(id);
        return ResponseEntity.ok(stationService.validateStation(stationId));
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<StationDashboardSummaryResponse> getDashboardSummary(){
        return ResponseEntity.ok(stationService.getDashboardSummary());
    }
}
