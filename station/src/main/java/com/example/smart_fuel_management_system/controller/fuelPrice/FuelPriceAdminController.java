package com.example.smart_fuel_management_system.controller.fuelPrice;


import com.example.smart_fuel_management_system.dto.fuelPrice.FuelPriceRequest;
import com.example.smart_fuel_management_system.dto.fuelPrice.FuelPriceResponse;
import com.example.smart_fuel_management_system.enums.FuelPriceStatusType;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.service.FuelPriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/admin/stations/{stationId}/fuel-prices")
public class FuelPriceAdminController {

    private final FuelPriceService fuelPriceService;

    @PostMapping("/{fuelType}")
    public ResponseEntity<Void> create(@PathVariable("stationId") String id,
                                       @PathVariable("fuelType") FuelType fuelType,
                                       @Valid @RequestBody FuelPriceRequest request) {
        UUID stationId = UUID.fromString(id);
        fuelPriceService.create(stationId, fuelType, request);
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<FuelPriceResponse>> getHistory(@PathVariable("stationId") String id){
        UUID stationId = UUID.fromString(id);
        return ResponseEntity.ok(fuelPriceService.getStationHistoryPrices(stationId));
    }

    @GetMapping
    public ResponseEntity<Page<FuelPriceResponse>> searchStationPrices(
            @PathVariable String stationId,
            @RequestParam(required = false) FuelType fuelType,
            @RequestParam(required = false) FuelPriceStatusType status,
            Pageable pageable
    ) {
        UUID id = UUID.fromString(stationId);
        return ResponseEntity.ok(
                fuelPriceService.searchStationPrices(
                        id,
                        fuelType,
                        status,
                        pageable
                )
        );
    }
}
