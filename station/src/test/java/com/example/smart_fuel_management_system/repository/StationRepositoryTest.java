package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.dto.station.StationTransactionResponse;
import com.example.smart_fuel_management_system.entity.Station;
import com.example.smart_fuel_management_system.enums.StationStatusType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class StationRepositoryTest {

    @Autowired
    private StationRepository stationRepository;

    private UUID activeStationOneId;
    private UUID activeStationTwoId;
    private UUID inactiveStationId;

    @BeforeEach
    void setUp() {
        activeStationOneId = persistStation(
                "Central Station",
                "Istanbul",
                "100 Main Street",
                StationStatusType.ACTIVE);

        activeStationTwoId = persistStation(
                "Airport Station",
                "Ankara",
                "500 Airport Road",
                StationStatusType.ACTIVE);

        inactiveStationId = persistStation(
                "Old Station",
                "Izmir",
                "1 Closed Street",
                StationStatusType.INACTIVE);
    }

    @Test
    void countByStatus_shouldReturnCount_whenStationsMatchStatus() {
        // given
        StationStatusType searchStatus = StationStatusType.ACTIVE;

        // when
        long count = stationRepository.countByStatus(searchStatus);

        // then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void countByStatus_shouldReturnZero_whenNoStationsMatchStatus() {
        // given
        StationStatusType searchStatus = StationStatusType.INACTIVE;

        // when
        long count = stationRepository.countByStatus(searchStatus);

        // then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void searchAndFilter_shouldReturnAllStations_whenSearchIsNullAndStatusIsNull() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Station> result = stationRepository.searchAndFilter(null, null, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    void searchAndFilter_shouldReturnAllStations_whenSearchIsBlankAndStatusIsNull() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Station> result = stationRepository.searchAndFilter("   ", null, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    void searchAndFilter_shouldMatchByName_whenSearchMatchesName() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Station> result = stationRepository.searchAndFilter("Central", null, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(activeStationOneId);
    }

    @Test
    void searchAndFilter_shouldMatchByCity_whenSearchMatchesCity() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Station> result = stationRepository.searchAndFilter("Ankara", null, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(activeStationTwoId);
    }

    @Test
    void searchAndFilter_shouldMatchByAddress_whenSearchMatchesAddress() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Station> result = stationRepository.searchAndFilter("Main Street", null, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(activeStationOneId);
    }

    @Test
    void searchAndFilter_shouldBeCaseInsensitive() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Station> result = stationRepository.searchAndFilter("central", null, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(activeStationOneId);
    }

    @Test
    void searchAndFilter_shouldReturnEmpty_whenSearchMatchesNothing() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Station> result = stationRepository.searchAndFilter("Nonexistent", null, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void searchAndFilter_shouldFilterByStatus_whenOnlyStatusProvided() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Station> result = stationRepository.searchAndFilter(
                null, StationStatusType.INACTIVE, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(inactiveStationId);
    }

    @Test
    void searchAndFilter_shouldApplyBothSearchAndStatus_whenBothProvided() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Station> result = stationRepository.searchAndFilter(
                "Station", StationStatusType.ACTIVE, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .allMatch(s -> s.getStatus() == StationStatusType.ACTIVE);
    }

    @Test
    void searchAndFilter_shouldReturnEmpty_whenSearchMatchesButStatusDoesNot() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Station> result = stationRepository.searchAndFilter(
                "Central", StationStatusType.INACTIVE, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void searchAndFilter_shouldRespectPagination() {
        // given
        Pageable firstPage = PageRequest.of(0, 2);

        // when
        Page<Station> result = stationRepository.searchAndFilter(null, null, firstPage);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void findStationsByIds_shouldReturnMatchingResponses_whenIdsExist() {
        // given
        List<UUID> searchIds = List.of(activeStationOneId, inactiveStationId);

        // when
        List<StationTransactionResponse> result =
                stationRepository.findStationsByIds(searchIds);

        // then
        assertThat(result)
                .hasSize(2)
                .extracting(StationTransactionResponse::id)
                .containsExactlyInAnyOrder(activeStationOneId, inactiveStationId);
    }

    @Test
    void findStationsByIds_shouldReturnResponseFields_whenStationExists() {
        // given
        List<UUID> searchIds = List.of(activeStationOneId);

        // when
        List<StationTransactionResponse> result =
                stationRepository.findStationsByIds(searchIds);

        // then
        assertThat(result).hasSize(1);
        StationTransactionResponse response = result.get(0);
        assertThat(response.id()).isEqualTo(activeStationOneId);
        assertThat(response.name()).isEqualTo("Central Station");
        assertThat(response.city()).isEqualTo("Istanbul");
        assertThat(response.address()).isEqualTo("100 Main Street");
    }

    @Test
    void findStationsByIds_shouldReturnEmptyList_whenNoIdsMatch() {
        // given
        List<UUID> searchIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        // when
        List<StationTransactionResponse> result =
                stationRepository.findStationsByIds(searchIds);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findStationsByIds_shouldReturnEmptyList_whenIdsListIsEmpty() {
        // given
        List<UUID> searchIds = List.of();

        // when
        List<StationTransactionResponse> result =
                stationRepository.findStationsByIds(searchIds);

        // then
        assertThat(result).isEmpty();
    }

    private UUID persistStation(
            String name,
            String city,
            String address,
            StationStatusType status) {

        Station station = Station.builder()
                .id(UUID.randomUUID())
                .name(name)
                .city(city)
                .address(address)
                .contactInformation("+90 555 000 0000")
                .latitude(BigDecimal.valueOf(41.0082))
                .longitude(BigDecimal.valueOf(28.9784))
                .status(status)
                .createdAt(LocalDateTime.of(2026, 9, 10, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 9, 10, 10, 0))
                .build();
        return stationRepository.save(station).getId();
    }
}