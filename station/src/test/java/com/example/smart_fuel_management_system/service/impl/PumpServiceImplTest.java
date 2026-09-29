package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.pump.CreatePumpRequest;
import com.example.smart_fuel_management_system.dto.pump.PumpResponse;
import com.example.smart_fuel_management_system.dto.station.StationFuelSessionResponse;
import com.example.smart_fuel_management_system.entity.Pump;
import com.example.smart_fuel_management_system.entity.Station;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.repository.PumpRepository;
import com.example.smart_fuel_management_system.service.PumpHeartbeatService;
import com.example.smart_fuel_management_system.service.StationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PumpServiceImplTest {

    @Mock
    private PumpRepository pumpRepository;

    @Mock
    private StationService stationService;

    @Mock
    private PumpHeartbeatService pumpHeartbeatService;

    @InjectMocks
    private PumpServiceImpl pumpService;

    private UUID stationId;
    private Station station;

    @BeforeEach
    void setUp() {
        stationId = UUID.randomUUID();
        station = mock(Station.class);
    }

    @Test
    void create_shouldThrow_whenElectricCombinedWithOtherFuelTypes() {
        // given
        CreatePumpRequest request = new CreatePumpRequest(
                Set.of(FuelType.ELECTRIC, FuelType.GASOLINE));

        // when / then
        assertThatThrownBy(() -> pumpService.create(stationId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ELECTRIC fuel type cannot be combined with other fuel types");

        verifyNoInteractions(pumpRepository, stationService, pumpHeartbeatService);
    }

    @Test
    void create_shouldAllowElectricAlone() {
        // given
        CreatePumpRequest request = new CreatePumpRequest(Set.of(FuelType.ELECTRIC));
        PumpResponse expected = mock(PumpResponse.class);

        when(stationService.getStation(stationId)).thenReturn(station);
        when(pumpRepository.findMaxPumpNumberByStationId(stationId)).thenReturn(0L);
        when(pumpRepository.save(org.mockito.ArgumentMatchers.any(Pump.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(pumpHeartbeatService.enrich(org.mockito.ArgumentMatchers.any(Pump.class)))
                .thenReturn(expected);

        // when
        PumpResponse result = pumpService.create(stationId, request);

        // then
        assertThat(result).isSameAs(expected);
    }

    @Test
    void create_shouldAssignNextPumpNumber_whenPumpsAlreadyExist() {
        // given
        CreatePumpRequest request = new CreatePumpRequest(Set.of(FuelType.GASOLINE));
        PumpResponse expected = mock(PumpResponse.class);

        when(stationService.getStation(stationId)).thenReturn(station);
        when(pumpRepository.findMaxPumpNumberByStationId(stationId)).thenReturn(5L);
        when(pumpRepository.save(org.mockito.ArgumentMatchers.any(Pump.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(pumpHeartbeatService.enrich(org.mockito.ArgumentMatchers.any(Pump.class)))
                .thenReturn(expected);

        // when
        pumpService.create(stationId, request);

        // then
        ArgumentCaptor<Pump> captor = ArgumentCaptor.forClass(Pump.class);
        verify(pumpRepository).save(captor.capture());

        Pump saved = captor.getValue();
        assertThat(saved.getPumpNumber()).isEqualTo(6L);
        assertThat(saved.getFuelTypes()).containsExactly(FuelType.GASOLINE);
        assertThat(saved.getStation()).isSameAs(station);
    }

    @Test
    void create_shouldAssignFirstPumpNumber_whenStationHasNoPumps() {
        // given
        CreatePumpRequest request = new CreatePumpRequest(Set.of(FuelType.DIESEL));
        PumpResponse expected = mock(PumpResponse.class);

        when(stationService.getStation(stationId)).thenReturn(station);
        when(pumpRepository.findMaxPumpNumberByStationId(stationId)).thenReturn(0L);
        when(pumpRepository.save(org.mockito.ArgumentMatchers.any(Pump.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(pumpHeartbeatService.enrich(org.mockito.ArgumentMatchers.any(Pump.class)))
                .thenReturn(expected);

        // when
        pumpService.create(stationId, request);

        // then
        ArgumentCaptor<Pump> captor = ArgumentCaptor.forClass(Pump.class);
        verify(pumpRepository).save(captor.capture());

        assertThat(captor.getValue().getPumpNumber()).isEqualTo(1L);
    }

    @Test
    void create_shouldCopyFuelTypesIntoMutableSet() {
        // given
        CreatePumpRequest request = new CreatePumpRequest(Set.of(FuelType.GASOLINE, FuelType.DIESEL));
        PumpResponse expected = mock(PumpResponse.class);

        when(stationService.getStation(stationId)).thenReturn(station);
        when(pumpRepository.findMaxPumpNumberByStationId(stationId)).thenReturn(0L);
        when(pumpRepository.save(org.mockito.ArgumentMatchers.any(Pump.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(pumpHeartbeatService.enrich(org.mockito.ArgumentMatchers.any(Pump.class)))
                .thenReturn(expected);

        // when
        pumpService.create(stationId, request);

        // then
        ArgumentCaptor<Pump> captor = ArgumentCaptor.forClass(Pump.class);
        verify(pumpRepository).save(captor.capture());

        assertThat(captor.getValue().getFuelTypes())
                .containsExactlyInAnyOrder(FuelType.GASOLINE, FuelType.DIESEL);
    }

    @Test
    void create_shouldReturnEnrichedResponse_whenSaveSucceeds() {
        // given
        CreatePumpRequest request = new CreatePumpRequest(Set.of(FuelType.GASOLINE));
        PumpResponse expected = mock(PumpResponse.class);

        when(stationService.getStation(stationId)).thenReturn(station);
        when(pumpRepository.findMaxPumpNumberByStationId(stationId)).thenReturn(0L);
        when(pumpRepository.save(org.mockito.ArgumentMatchers.any(Pump.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(pumpHeartbeatService.enrich(org.mockito.ArgumentMatchers.any(Pump.class)))
                .thenReturn(expected);

        // when
        PumpResponse result = pumpService.create(stationId, request);

        // then
        assertThat(result).isSameAs(expected);
        verify(pumpHeartbeatService).enrich(org.mockito.ArgumentMatchers.any(Pump.class));
    }

    @Test
    void create_shouldNotSave_whenElectricValidationFails() {
        // given
        CreatePumpRequest request = new CreatePumpRequest(
                Set.of(FuelType.ELECTRIC, FuelType.DIESEL));

        // when / then
        assertThatThrownBy(() -> pumpService.create(stationId, request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(pumpRepository, never()).save(org.mockito.ArgumentMatchers.any(Pump.class));
    }

    @Test
    void getStationId_shouldDelegateToRepository() {
        // given
        UUID pumpId = UUID.randomUUID();
        StationFuelSessionResponse expected = mock(StationFuelSessionResponse.class);
        when(pumpRepository.findStationIdAndPumpIdById(pumpId)).thenReturn(expected);

        // when
        StationFuelSessionResponse result = pumpService.getStationId(pumpId);

        // then
        assertThat(result).isSameAs(expected);
        verify(pumpRepository).findStationIdAndPumpIdById(pumpId);
    }

    @Test
    void getStationId_shouldReturnNull_whenRepositoryReturnsNull() {
        // given
        UUID pumpId = UUID.randomUUID();
        when(pumpRepository.findStationIdAndPumpIdById(pumpId)).thenReturn(null);

        // when
        StationFuelSessionResponse result = pumpService.getStationId(pumpId);

        // then
        assertThat(result).isNull();
    }
}