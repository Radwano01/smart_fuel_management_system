package com.example.smart_fuel_management_system.controller.fuelPrice;

import com.example.smart_fuel_management_system.dto.fuelPrice.FuelPriceResponse;
import com.example.smart_fuel_management_system.service.FuelPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

//STATION + ADMIN ONLY HAS CONTROL ON THIS CONTROLLER
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/stations/{stationId}/fuel-types")
public class FuelPriceController {

    private final FuelPriceService fuelPriceService;

    @GetMapping
    public ResponseEntity<List<FuelPriceResponse>> getPrice(@PathVariable("stationId") String id){
        UUID stationId = UUID.fromString(id);
        return ResponseEntity.ok(fuelPriceService.getStationPrices(stationId));
    }
}
