package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.fuelPrice.FuelPriceRequest;
import com.example.smart_fuel_management_system.dto.fuelPrice.FuelPriceResponse;
import com.example.smart_fuel_management_system.entity.FuelPrice;
import com.example.smart_fuel_management_system.entity.Station;
import com.example.smart_fuel_management_system.enums.FuelPriceStatusType;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.repository.FuelPriceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FuelPriceServiceImplTest {

    @Mock
    private FuelPriceRepository fuelPriceRepository;

    @Mock
    private StationServiceImpl stationService;

    @InjectMocks
    private FuelPriceServiceImpl fuelPriceService;

    private UUID stationId;
    private Station station;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        stationId = UUID.randomUUID();
        station = mock(Station.class);
        baseTime = LocalDateTime.of(2026, 9, 10, 10, 0);
    }

    @Test
    void create_shouldSaveNewActivePrice_whenNoExistingActivePrice() {
        // given
        FuelType fuelType = FuelType.GASOLINE;
        FuelPriceRequest request = new FuelPriceRequest(new BigDecimal("50.00"));

        when(stationService.getStation(stationId)).thenReturn(station);
        when(fuelPriceRepository.findByStationIdAndFuelTypeAndFuelPriceStatusType(
                stationId, fuelType, FuelPriceStatusType.ACTIVE))
                .thenReturn(Optional.empty());

        // when
        fuelPriceService.create(stationId, fuelType, request);

        // then
        ArgumentCaptor<FuelPrice> captor = ArgumentCaptor.forClass(FuelPrice.class);
        verify(fuelPriceRepository).save(captor.capture());

        FuelPrice saved = captor.getValue();
        assertThat(saved.getFuelType()).isEqualTo(fuelType);
        assertThat(saved.getPrice()).isEqualByComparingTo("50.00");
        assertThat(saved.getStation()).isSameAs(station);
        assertThat(saved.getFuelPriceStatusType()).isEqualTo(FuelPriceStatusType.ACTIVE);
    }

    @Test
    void create_shouldDeactivateExistingActivePrice_whenPriceAlreadyExists() {
        // given
        FuelType fuelType = FuelType.GASOLINE;
        FuelPriceRequest request = new FuelPriceRequest(new BigDecimal("55.00"));
        FuelPrice existing = mock(FuelPrice.class);

        when(stationService.getStation(stationId)).thenReturn(station);
        when(fuelPriceRepository.findByStationIdAndFuelTypeAndFuelPriceStatusType(
                stationId, fuelType, FuelPriceStatusType.ACTIVE))
                .thenReturn(Optional.of(existing));

        // when
        fuelPriceService.create(stationId, fuelType, request);

        // then
        verify(existing).setFuelPriceStatusType(FuelPriceStatusType.INACTIVE);
        verify(fuelPriceRepository).save(any(FuelPrice.class));
    }

    @Test
    void getPrice_shouldReturnPrice_whenActivePriceExists() {
        // given
        FuelType fuelType = FuelType.GASOLINE;
        BigDecimal expected = new BigDecimal("50.00");
        FuelPrice active = mock(FuelPrice.class);
        when(active.getPrice()).thenReturn(expected);

        when(fuelPriceRepository.findByStationIdAndFuelTypeAndFuelPriceStatusType(
                stationId, fuelType, FuelPriceStatusType.ACTIVE))
                .thenReturn(Optional.of(active));

        // when
        BigDecimal result = fuelPriceService.getPrice(stationId, fuelType);

        // then
        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    void getPrice_shouldThrow_whenNoActivePriceExists() {
        // given
        FuelType fuelType = FuelType.GASOLINE;

        when(fuelPriceRepository.findByStationIdAndFuelTypeAndFuelPriceStatusType(
                stationId, fuelType, FuelPriceStatusType.ACTIVE))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> fuelPriceService.getPrice(stationId, fuelType))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Fuel price not found");
    }

    @Test
    void getStationHistoryPrices_shouldDelegateToRepository() {
        // given
        List<FuelPriceResponse> expected = List.of(mock(FuelPriceResponse.class));
        when(fuelPriceRepository.findStationPriceHistory(stationId)).thenReturn(expected);

        // when
        List<FuelPriceResponse> result = fuelPriceService.getStationHistoryPrices(stationId);

        // then
        assertThat(result).isSameAs(expected);
        verify(fuelPriceRepository).findStationPriceHistory(stationId);
    }

    @Test
    void searchStationPrices_shouldDelegateToRepository() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<FuelPriceResponse> expected = new PageImpl<>(List.of());
        when(fuelPriceRepository.searchStationPrices(
                stationId, FuelType.GASOLINE, FuelPriceStatusType.ACTIVE, pageable))
                .thenReturn(expected);

        // when
        Page<FuelPriceResponse> result = fuelPriceService.searchStationPrices(
                stationId, FuelType.GASOLINE, FuelPriceStatusType.ACTIVE, pageable);

        // then
        assertThat(result).isSameAs(expected);
        verify(fuelPriceRepository).searchStationPrices(
                stationId, FuelType.GASOLINE, FuelPriceStatusType.ACTIVE, pageable);
    }

    @Test
    void getStationPrices_shouldMapActivePricesToResponses() {
        // given
        UUID priceId = UUID.randomUUID();
        FuelPrice active = mock(FuelPrice.class);
        when(active.getId()).thenReturn(priceId);
        when(active.getFuelType()).thenReturn(FuelType.GASOLINE);
        when(active.getPrice()).thenReturn(new BigDecimal("50.00"));
        when(active.getCreatedAt()).thenReturn(baseTime);

        when(fuelPriceRepository.findByStationIdAndFuelPriceStatusType(
                stationId, FuelPriceStatusType.ACTIVE))
                .thenReturn(List.of(active));

        // when
        List<FuelPriceResponse> result = fuelPriceService.getStationPrices(stationId);

        // then
        assertThat(result).hasSize(1);
        FuelPriceResponse response = result.get(0);
        assertThat(response.id()).isEqualTo(priceId);
        assertThat(response.fuelType()).isEqualTo(FuelType.GASOLINE);
        assertThat(response.price()).isEqualByComparingTo("50.00");
        assertThat(response.createdAt()).isEqualTo(baseTime);
    }

    @Test
    void getStationPrices_shouldReturnEmptyList_whenStationHasNoActivePrices() {
        // given
        when(fuelPriceRepository.findByStationIdAndFuelPriceStatusType(
                stationId, FuelPriceStatusType.ACTIVE))
                .thenReturn(List.of());

        // when
        List<FuelPriceResponse> result = fuelPriceService.getStationPrices(stationId);

        // then
        assertThat(result).isEmpty();
    }
}