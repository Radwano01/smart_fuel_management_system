package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.fuelPrice.FuelPriceRequest;
import com.example.smart_fuel_management_system.dto.fuelPrice.FuelPriceResponse;
import com.example.smart_fuel_management_system.entity.FuelPrice;
import com.example.smart_fuel_management_system.entity.Station;
import com.example.smart_fuel_management_system.enums.FuelPriceStatusType;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.repository.FuelPriceRepository;
import com.example.smart_fuel_management_system.service.FuelPriceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FuelPriceServiceImpl implements FuelPriceService {

    private final FuelPriceRepository fuelPriceRepository;
    private final StationServiceImpl stationService;

    @Transactional
    @Override
    public void create(UUID stationId, FuelType fuelType, FuelPriceRequest request){
        Station station = stationService.getStation(stationId);

        fuelPriceRepository
                .findByStationIdAndFuelTypeAndFuelPriceStatusType(
                        stationId,
                        fuelType,
                        FuelPriceStatusType.ACTIVE
                )
                .ifPresent(fp ->
                        fp.setFuelPriceStatusType(FuelPriceStatusType.INACTIVE)
                );

        FuelPrice fuelPrice = new FuelPrice(
                fuelType,
                request.price(),
                station,
                FuelPriceStatusType.ACTIVE
        );

        fuelPriceRepository.save(fuelPrice);
    }

    @Transactional(readOnly = true)
    @Override
    public BigDecimal getPrice(UUID stationId, FuelType fuelType) {
        return fuelPriceRepository
                .findByStationIdAndFuelTypeAndFuelPriceStatusType(stationId, fuelType, FuelPriceStatusType.ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("Fuel price not found"))
                .getPrice();
    }

    @Transactional(readOnly = true)
    @Override
    public List<FuelPriceResponse> getStationHistoryPrices(UUID stationId){
        return fuelPriceRepository.findStationPriceHistory(stationId);
    }


    @Transactional(readOnly = true)
    @Override
    public Page<FuelPriceResponse> searchStationPrices(
            UUID stationId,
            FuelType fuelType,
            FuelPriceStatusType status,
            Pageable pageable
    ) {
        return fuelPriceRepository.searchStationPrices(
                stationId,
                fuelType,
                status,
                pageable
        );
    }

    @Override
    public List<FuelPriceResponse> getStationPrices(UUID stationId) {
        return fuelPriceRepository
                .findByStationIdAndFuelPriceStatusType(
                        stationId,
                        FuelPriceStatusType.ACTIVE
                )
                .stream()
                .map(price -> new FuelPriceResponse(
                        price.getId(),
                        price.getFuelType(),
                        price.getPrice(),
                        price.getCreatedAt()
                ))
                .toList();
    }
}
