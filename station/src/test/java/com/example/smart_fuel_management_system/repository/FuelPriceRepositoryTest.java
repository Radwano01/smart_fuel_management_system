package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.dto.fuelPrice.FuelPriceResponse;
import com.example.smart_fuel_management_system.entity.FuelPrice;
import com.example.smart_fuel_management_system.entity.Station;
import com.example.smart_fuel_management_system.enums.FuelPriceStatusType;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.enums.StationStatusType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FuelPriceRepositoryTest {

    @Autowired
    private FuelPriceRepository fuelPriceRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID stationId;
    private UUID otherStationId;
    private LocalDateTime baseTime;

    private FuelPrice activeGasolineId;
    private FuelPrice activeDieselId;
    private FuelPrice inactiveGasolineId;
    private FuelPrice otherStationActiveGasolineId;

    @BeforeEach
    void setUp() {
        baseTime = LocalDateTime.of(2026, 9, 10, 10, 0);

        stationId = persistStation("Station A");
        otherStationId = persistStation("Station B");

        FuelPrice fp1 = fuelPriceRepository.save(buildFuelPrice(
                stationId, FuelType.GASOLINE, new BigDecimal("50.00"),
                FuelPriceStatusType.ACTIVE));
        activeGasolineId = fp1;

        FuelPrice fp2 = fuelPriceRepository.save(buildFuelPrice(
                stationId, FuelType.DIESEL, new BigDecimal("45.00"),
                FuelPriceStatusType.ACTIVE));
        activeDieselId = fp2;

        FuelPrice fp3 = fuelPriceRepository.save(buildFuelPrice(
                stationId, FuelType.GASOLINE, new BigDecimal("48.00"),
                FuelPriceStatusType.INACTIVE));
        inactiveGasolineId = fp3;

        FuelPrice fp4 = fuelPriceRepository.save(buildFuelPrice(
                otherStationId, FuelType.GASOLINE, new BigDecimal("52.00"),
                FuelPriceStatusType.ACTIVE));
        otherStationActiveGasolineId = fp4;

        entityManager.flush();
        forceCreatedAt(activeGasolineId.getId(), baseTime);
        forceCreatedAt(activeDieselId.getId(), baseTime.plusHours(1));
        forceCreatedAt(inactiveGasolineId.getId(), baseTime.plusHours(2));
        forceCreatedAt(otherStationActiveGasolineId.getId(), baseTime.plusHours(3));
        entityManager.clear();
    }

    @Test
    void findByStationIdAndFuelTypeAndFuelPriceStatusType_shouldReturnPrice_whenAllFieldsMatch() {
        // given
        UUID searchStationId = stationId;
        FuelType searchFuelType = FuelType.GASOLINE;
        FuelPriceStatusType searchStatus = FuelPriceStatusType.ACTIVE;

        // when
        Optional<FuelPrice> result = fuelPriceRepository
                .findByStationIdAndFuelTypeAndFuelPriceStatusType(
                        searchStationId, searchFuelType, searchStatus);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getPrice()).isEqualByComparingTo("50.00");
        assertThat(result.get().getFuelPriceStatusType()).isEqualTo(FuelPriceStatusType.ACTIVE);
    }

    @Test
    void findByStationIdAndFuelTypeAndFuelPriceStatusType_shouldReturnEmpty_whenStationDoesNotMatch() {
        // given
        UUID unknownStationId = UUID.randomUUID();

        // when
        Optional<FuelPrice> result = fuelPriceRepository
                .findByStationIdAndFuelTypeAndFuelPriceStatusType(
                        unknownStationId, FuelType.GASOLINE, FuelPriceStatusType.ACTIVE);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByStationIdAndFuelTypeAndFuelPriceStatusType_shouldReturnEmpty_whenFuelTypeDoesNotMatch() {
        // given
        UUID searchStationId = stationId;
        FuelType unmatched = FuelType.HYBRID;

        // when
        Optional<FuelPrice> result = fuelPriceRepository
                .findByStationIdAndFuelTypeAndFuelPriceStatusType(
                        searchStationId, unmatched, FuelPriceStatusType.ACTIVE);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByStationIdAndFuelTypeAndFuelPriceStatusType_shouldReturnEmpty_whenStatusDoesNotMatch() {
        // given
        UUID searchStationId = stationId;
        FuelType searchFuelType = FuelType.DIESEL;
        FuelPriceStatusType searchStatus = FuelPriceStatusType.INACTIVE;

        // when
        Optional<FuelPrice> result = fuelPriceRepository
                .findByStationIdAndFuelTypeAndFuelPriceStatusType(
                        searchStationId, searchFuelType, searchStatus);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findStationPriceHistory_shouldReturnOnlyInactivePrices_whenStationHasBothStatuses() {
        // given
        UUID searchStationId = stationId;

        // when
        List<FuelPriceResponse> result =
                fuelPriceRepository.findStationPriceHistory(searchStationId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(inactiveGasolineId.getId());
        assertThat(result.get(0).fuelType()).isEqualTo(FuelType.GASOLINE);
        assertThat(result.get(0).price()).isEqualByComparingTo("48.00");
    }

    @Test
    void findStationPriceHistory_shouldOrderByCreatedAtDesc() {
        // given
        FuelPrice older = fuelPriceRepository.save(buildFuelPrice(
                stationId, FuelType.DIESEL, new BigDecimal("44.00"),
                FuelPriceStatusType.INACTIVE));
        entityManager.flush();
        forceCreatedAt(older.getId(), baseTime.minusHours(1));
        entityManager.clear();

        // when
        List<FuelPriceResponse> result =
                fuelPriceRepository.findStationPriceHistory(stationId);

        // then
        assertThat(result)
                .extracting(FuelPriceResponse::id)
                .containsExactly(inactiveGasolineId.getId(), older.getId());
    }

    @Test
    void findStationPriceHistory_shouldReturnEmpty_whenStationHasNoInactivePrices() {
        // given
        UUID searchStationId = otherStationId;

        // when
        List<FuelPriceResponse> result =
                fuelPriceRepository.findStationPriceHistory(searchStationId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findStationPriceHistory_shouldReturnEmpty_whenStationDoesNotExist() {
        // given
        UUID unknownStationId = UUID.randomUUID();

        // when
        List<FuelPriceResponse> result =
                fuelPriceRepository.findStationPriceHistory(unknownStationId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void searchStationPrices_shouldReturnAllStationPrices_whenBothFiltersAreNull() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<FuelPriceResponse> result = fuelPriceRepository.searchStationPrices(
                stationId, null, null, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    void searchStationPrices_shouldFilterByFuelType_whenOnlyFuelTypeProvided() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<FuelPriceResponse> result = fuelPriceRepository.searchStationPrices(
                stationId, FuelType.GASOLINE, null, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .allMatch(r -> r.fuelType() == FuelType.GASOLINE);
    }

    @Test
    void searchStationPrices_shouldFilterByStatus_whenOnlyStatusProvided() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<FuelPriceResponse> result = fuelPriceRepository.searchStationPrices(
                stationId, null, FuelPriceStatusType.INACTIVE, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).id())
                .isEqualTo(inactiveGasolineId.getId());
    }

    @Test
    void searchStationPrices_shouldFilterByBoth_whenBothProvided() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<FuelPriceResponse> result = fuelPriceRepository.searchStationPrices(
                stationId, FuelType.DIESEL, FuelPriceStatusType.ACTIVE, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).id())
                .isEqualTo(activeDieselId.getId());
    }

    @Test
    void searchStationPrices_shouldReturnEmpty_whenNoMatchesWithFilters() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<FuelPriceResponse> result = fuelPriceRepository.searchStationPrices(
                stationId, FuelType.HYBRID, FuelPriceStatusType.ACTIVE, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void searchStationPrices_shouldReturnEmpty_whenStationHasNoPrices() {
        // given
        UUID unknownStationId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<FuelPriceResponse> result = fuelPriceRepository.searchStationPrices(
                unknownStationId, null, null, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void searchStationPrices_shouldOrderByCreatedAtDesc() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<FuelPriceResponse> result = fuelPriceRepository.searchStationPrices(
                stationId, null, null, pageable);

        // then
        assertThat(result.getContent())
                .extracting(FuelPriceResponse::id)
                .containsExactly(
                        inactiveGasolineId.getId(),
                        activeDieselId.getId(),
                        activeGasolineId.getId());
    }

    @Test
    void searchStationPrices_shouldRespectPagination() {
        // given
        Pageable firstPage = PageRequest.of(0, 2);

        // when
        Page<FuelPriceResponse> result = fuelPriceRepository.searchStationPrices(
                stationId, null, null, firstPage);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void findByStationIdAndFuelPriceStatusType_shouldReturnMatchingPrices_whenStatusMatches() {
        // given
        UUID searchStationId = stationId;
        FuelPriceStatusType searchStatus = FuelPriceStatusType.ACTIVE;

        // when
        List<FuelPrice> result = fuelPriceRepository
                .findByStationIdAndFuelPriceStatusType(searchStationId, searchStatus);

        // then
        assertThat(result)
                .hasSize(2)
                .allMatch(fp -> fp.getFuelPriceStatusType() == FuelPriceStatusType.ACTIVE);
    }

    @Test
    void findByStationIdAndFuelPriceStatusType_shouldReturnEmpty_whenStationDoesNotMatch() {
        // given
        UUID unknownStationId = UUID.randomUUID();

        // when
        List<FuelPrice> result = fuelPriceRepository
                .findByStationIdAndFuelPriceStatusType(
                        unknownStationId, FuelPriceStatusType.ACTIVE);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByStationIdAndFuelPriceStatusType_shouldReturnEmpty_whenNoMatchingStatus() {
        // given
        UUID searchStationId = otherStationId;
        FuelPriceStatusType searchStatus = FuelPriceStatusType.INACTIVE;

        // when
        List<FuelPrice> result = fuelPriceRepository
                .findByStationIdAndFuelPriceStatusType(searchStationId, searchStatus);

        // then
        assertThat(result).isEmpty();
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
                .createdAt(baseTime)
                .updatedAt(baseTime)
                .build();
        Station managed = entityManager.merge(station);
        return managed.getId();
    }

    private FuelPrice buildFuelPrice(
            UUID stationId,
            FuelType fuelType,
            BigDecimal price,
            FuelPriceStatusType status) {

        Station stationRef = entityManager.getReference(Station.class, stationId);

        return FuelPrice.builder()
                .station(stationRef)
                .fuelType(fuelType)
                .price(price)
                .fuelPriceStatusType(status)
                .build();
    }

    private void forceCreatedAt(UUID id, LocalDateTime ts) {
        entityManager.createNativeQuery(
                        "UPDATE fuel_prices SET " + createdAtColumn() + " = :ts WHERE id = :id")
                .setParameter("ts", ts)
                .setParameter("id", id)
                .executeUpdate();
    }

    private String createdAtColumn() {
        return "created_at";
    }
}