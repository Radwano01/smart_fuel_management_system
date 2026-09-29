package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.entity.Station;
import com.example.smart_fuel_management_system.entity.StationEmployee;
import com.example.smart_fuel_management_system.enums.StationStatusType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class StationEmployeeRepositoryTest {

    @Autowired
    private StationEmployeeRepository stationEmployeeRepository;

    @Autowired
    private StationRepository stationRepository;

    private UUID stationOneId;
    private UUID stationTwoId;
    private UUID employeeOneId;
    private UUID employeeTwoId;

    @BeforeEach
    void setUp() {
        stationOneId = persistStation("Station A");
        stationTwoId = persistStation("Station B");

        employeeOneId = UUID.randomUUID();
        employeeTwoId = UUID.randomUUID();

        stationEmployeeRepository.save(buildStationEmployee(employeeOneId, stationOneId));
        stationEmployeeRepository.save(buildStationEmployee(employeeTwoId, stationTwoId));
    }

    @Test
    void existsByStationId_shouldReturnTrue_whenStationHasEmployee() {
        // given
        UUID searchStationId = stationOneId;

        // when
        boolean exists = stationEmployeeRepository.existsByStationId(searchStationId);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByStationId_shouldReturnFalse_whenStationHasNoEmployee() {
        // given
        UUID emptyStationId = persistStation("Empty Station");

        // when
        boolean exists = stationEmployeeRepository.existsByStationId(emptyStationId);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByStationId_shouldReturnFalse_whenStationDoesNotExist() {
        // given
        UUID unknownStationId = UUID.randomUUID();

        // when
        boolean exists = stationEmployeeRepository.existsByStationId(unknownStationId);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void findByEmployeeId_shouldReturnStation_whenEmployeeIsAssigned() {
        // given
        UUID searchEmployeeId = employeeOneId;

        // when
        Optional<Station> result =
                stationEmployeeRepository.findByEmployeeId(searchEmployeeId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(stationOneId);
    }

    @Test
    void findByEmployeeId_shouldReturnEmpty_whenEmployeeIsNotAssigned() {
        // given
        UUID unassignedEmployeeId = UUID.randomUUID();

        // when
        Optional<Station> result =
                stationEmployeeRepository.findByEmployeeId(unassignedEmployeeId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByStationId_shouldReturnEmployee_whenStationHasAssignment() {
        // given
        UUID searchStationId = stationOneId;

        // when
        Optional<StationEmployee> result =
                stationEmployeeRepository.findByStationId(searchStationId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmployeeId()).isEqualTo(employeeOneId);
        assertThat(result.get().getStation().getId()).isEqualTo(stationOneId);
    }

    @Test
    void findByStationId_shouldReturnEmpty_whenStationHasNoAssignment() {
        // given
        UUID emptyStationId = persistStation("Empty Station");

        // when
        Optional<StationEmployee> result =
                stationEmployeeRepository.findByStationId(emptyStationId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findStationEmployeeByEmployeeId_shouldReturnAssignment_whenEmployeeExists() {
        // given
        UUID searchEmployeeId = employeeTwoId;

        // when
        Optional<StationEmployee> result =
                stationEmployeeRepository.findStationEmployeeByEmployeeId(searchEmployeeId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmployeeId()).isEqualTo(searchEmployeeId);
        assertThat(result.get().getStation().getId()).isEqualTo(stationTwoId);
    }

    @Test
    void findStationEmployeeByEmployeeId_shouldReturnEmpty_whenEmployeeDoesNotExist() {
        // given
        UUID unknownEmployeeId = UUID.randomUUID();

        // when
        Optional<StationEmployee> result =
                stationEmployeeRepository.findStationEmployeeByEmployeeId(unknownEmployeeId);

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
                .createdAt(LocalDateTime.of(2026, 9, 10, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 9, 10, 10, 0))
                .build();
        return stationRepository.save(station).getId();
    }

    private StationEmployee buildStationEmployee(UUID employeeId, UUID stationId) {
        Station stationRef = stationRepository.getReferenceById(stationId);

        return StationEmployee.builder()
                .id(UUID.randomUUID())
                .employeeId(employeeId)
                .station(stationRef)
                .build();
    }
}