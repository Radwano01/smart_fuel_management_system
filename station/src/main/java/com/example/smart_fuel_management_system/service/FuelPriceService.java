package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.fuelPrice.FuelPriceRequest;
import com.example.smart_fuel_management_system.dto.fuelPrice.FuelPriceResponse;
import com.example.smart_fuel_management_system.enums.FuelPriceStatusType;
import com.example.smart_fuel_management_system.enums.FuelType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface FuelPriceService {
    void create(UUID stationId, FuelType fuelType, FuelPriceRequest request);
    BigDecimal getPrice(UUID stationId, FuelType fuelType);
    List<FuelPriceResponse> getStationHistoryPrices(UUID stationId);
    Page<FuelPriceResponse> searchStationPrices(
            UUID stationId,
            FuelType fuelType,
            FuelPriceStatusType status,
            Pageable pageable
    );
    List<FuelPriceResponse> getStationPrices(UUID stationId);
}
