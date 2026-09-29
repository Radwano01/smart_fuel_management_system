package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.dto.station.StationFuelSessionResponse;
import com.example.smart_fuel_management_system.entity.Pump;
import com.example.smart_fuel_management_system.entity.Station;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.enums.StationStatusType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PumpRepositoryTest {

    @Autowired
    private PumpRepository pumpRepository;

    @Autowired
    private StationRepository stationRepository;

    private UUID stationOneId;
    private UUID stationTwoId;

    @BeforeEach
    void setUp() {
        stationOneId = persistStation("Station A");
        stationTwoId = persistStation("Station B");

        persistPump(stationOneId, 1, FuelType.GASOLINE);
        persistPump(stationOneId, 2, FuelType.DIESEL);
        persistPump(stationOneId, 5, FuelType.GASOLINE);
        persistPump(stationTwoId, 3, FuelType.DIESEL);
    }

    @Test
    void findMaxPumpNumberByStationId_shouldReturnMax_whenStationHasPumps() {
        // given
        UUID searchStationId = stationOneId;

        // when
        long result = pumpRepository.findMaxPumpNumberByStationId(searchStationId);

        // then
        assertThat(result).isEqualTo(5L);
    }

    @Test
    void findMaxPumpNumberByStationId_shouldReturnMaxForThatStationOnly() {
        // given
        UUID searchStationId = stationTwoId;

        // when
        long result = pumpRepository.findMaxPumpNumberByStationId(searchStationId);

        // then
        assertThat(result).isEqualTo(3L);
    }

    @Test
    void findMaxPumpNumberByStationId_shouldReturnZero_whenStationHasNoPumps() {
        // given
        UUID emptyStationId = persistStation("Empty Station");

        // when
        long result = pumpRepository.findMaxPumpNumberByStationId(emptyStationId);

        // then
        assertThat(result).isZero();
    }

    @Test
    void findMaxPumpNumberByStationId_shouldReturnZero_whenStationDoesNotExist() {
        // given
        UUID unknownStationId = UUID.randomUUID();

        // when
        long result = pumpRepository.findMaxPumpNumberByStationId(unknownStationId);

        // then
        assertThat(result).isZero();
    }

    @Test
    void findStationIdAndPumpIdById_shouldReturnProjection_whenPumpExists() {
        // given
        UUID pumpId = persistPump(stationOneId, 7, FuelType.GASOLINE);

        // when
        StationFuelSessionResponse result =
                pumpRepository.findStationIdAndPumpIdById(pumpId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.stationId()).isEqualTo(stationOneId);
        assertThat(result.pumpId()).isEqualTo(pumpId);
    }

    @Test
    void findStationIdAndPumpIdById_shouldReturnNull_whenPumpDoesNotExist() {
        // given
        UUID unknownPumpId = UUID.randomUUID();

        // when
        StationFuelSessionResponse result =
                pumpRepository.findStationIdAndPumpIdById(unknownPumpId);

        // then
        assertThat(result).isNull();
    }

    private UUID persistStation(String name) {
        Station station = Station.builder()
                .id(UUID.randomUUID())
                .name(name)
                .address("123 Main Street")
                .city("Istanbul")
                .contactInformation("+90 555 000 0000")
                .latitude(BigDecimal.valueOf(41.0082))
                .longitude(BigDecimal.valueOf(28.9784))
                .status(StationStatusType.ACTIVE)
                .createdAt(LocalDateTime.of(2026, 9, 10, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 9, 10, 10, 0))
                .build();
        return stationRepository.save(station).getId();
    }

    private UUID persistPump(UUID stationId, int pumpNumber, FuelType fuelType) {
        Station stationRef = stationRepository.getReferenceById(stationId);

        Pump pump = Pump.builder()
                .id(UUID.randomUUID())
                .pumpNumber(pumpNumber)
                .fuelTypes(Collections.singleton(fuelType))
                .station(stationRef)
                .build();
        return pumpRepository.save(pump).getId();
    }
}