package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.station.SaveStationUser;
import com.example.smart_fuel_management_system.dto.station.StationEmployeeAuthResponse;
import com.example.smart_fuel_management_system.entity.Station;
import com.example.smart_fuel_management_system.entity.StationEmployee;
import com.example.smart_fuel_management_system.repository.StationEmployeeRepository;
import com.example.smart_fuel_management_system.service.StationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationEmployeeServiceImplTest {

    @Mock
    private StationEmployeeRepository stationEmployeeRepository;

    @Mock
    private StationService stationService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private StationEmployeeServiceImpl stationEmployeeService;

    private UUID employeeId;
    private UUID stationId;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        stationId = UUID.randomUUID();
    }

    @Test
    void create_shouldSaveEmployeeWithNullStation_whenPayloadIsValid() throws Exception {
        // given
        String payload = "{\"employeeId\":\"" + employeeId + "\"}";
        SaveStationUser parsed = new SaveStationUser(employeeId);
        when(objectMapper.readValue(payload, SaveStationUser.class)).thenReturn(parsed);

        // when
        stationEmployeeService.create(payload);

        // then
        ArgumentCaptor<StationEmployee> captor = ArgumentCaptor.forClass(StationEmployee.class);
        verify(stationEmployeeRepository).save(captor.capture());

        StationEmployee saved = captor.getValue();
        assertThat(saved.getEmployeeId()).isEqualTo(employeeId);
        assertThat(saved.getStation()).isNull();
    }

    @Test
    void create_shouldThrow_whenPayloadCannotBeParsed() throws Exception {
        // given
        String payload = "not-json";
        JsonProcessingException failure = mock(JsonProcessingException.class);
        when(objectMapper.readValue(payload, SaveStationUser.class)).thenThrow(failure);

        // when / then
        assertThatThrownBy(() -> stationEmployeeService.create(payload))
                .isSameAs(failure);

        verify(stationEmployeeRepository, never()).save(any(StationEmployee.class));
    }

    @Test
    void findStationByEmployeeId_shouldReturnStation_whenEmployeeExists() {
        // given
        Station station = mock(Station.class);
        when(stationEmployeeRepository.findByEmployeeId(employeeId))
                .thenReturn(Optional.of(station));

        // when
        Station result = stationEmployeeService.findStationByEmployeeId(employeeId);

        // then
        assertThat(result).isSameAs(station);
    }

    @Test
    void findStationByEmployeeId_shouldThrow_whenEmployeeDoesNotExist() {
        // given
        when(stationEmployeeRepository.findByEmployeeId(employeeId))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> stationEmployeeService.findStationByEmployeeId(employeeId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Employee does not exist");
    }

    @Test
    void getStationDetails_shouldReturnMappedResponse_whenEmployeeExists() {
        // given
        Station station = mock(Station.class);
        when(station.getId()).thenReturn(stationId);
        when(station.getName()).thenReturn("Central Station");
        when(station.getCity()).thenReturn("Istanbul");
        when(station.getAddress()).thenReturn("100 Main Street");

        when(stationEmployeeRepository.findByEmployeeId(employeeId))
                .thenReturn(Optional.of(station));

        // when
        StationEmployeeAuthResponse result =
                stationEmployeeService.getStationDetails(employeeId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(stationId);
        assertThat(result.name()).isEqualTo("Central Station");
        assertThat(result.city()).isEqualTo("Istanbul");
        assertThat(result.address()).isEqualTo("100 Main Street");
    }

    @Test
    void getStationDetails_shouldThrow_whenEmployeeDoesNotExist() {
        // given
        when(stationEmployeeRepository.findByEmployeeId(employeeId))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> stationEmployeeService.getStationDetails(employeeId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Employee does not exist");
    }

    @Test
    void changeStation_shouldThrow_whenEmployeeNotFound() {
        // given
        when(stationEmployeeRepository.findStationEmployeeByEmployeeId(employeeId))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> stationEmployeeService.changeStation(employeeId, stationId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Station employee not found");

        verify(stationService, never()).getStation(any(UUID.class));
    }

    @Test
    void changeStation_shouldThrow_whenEmployeeAlreadyAssignedToSameStation() {
        // given
        Station existingStation = mock(Station.class);
        when(existingStation.getId()).thenReturn(stationId);

        StationEmployee employee = mock(StationEmployee.class);
        when(employee.getStation()).thenReturn(existingStation);

        when(stationEmployeeRepository.findStationEmployeeByEmployeeId(employeeId))
                .thenReturn(Optional.of(employee));

        // when / then
        assertThatThrownBy(() -> stationEmployeeService.changeStation(employeeId, stationId))
                .isInstanceOf(EntityExistsException.class)
                .hasMessageContaining("Employee is already assigned to this station");

        verify(stationService, never()).getStation(any(UUID.class));
    }

    @Test
    void changeStation_shouldReassignEmployee_whenTargetStationIsFree() {
        // given
        Station targetStation = mock(Station.class);

        StationEmployee employee = mock(StationEmployee.class);
        when(employee.getStation()).thenReturn(null);

        when(stationEmployeeRepository.findStationEmployeeByEmployeeId(employeeId))
                .thenReturn(Optional.of(employee));
        when(stationEmployeeRepository.findByStationId(stationId))
                .thenReturn(Optional.empty());
        when(stationService.getStation(stationId)).thenReturn(targetStation);

        // when
        stationEmployeeService.changeStation(employeeId, stationId);

        // then
        verify(employee).setStation(targetStation);
    }

    @Test
    void changeStation_shouldUnassignPreviousOccupant_whenTargetStationAlreadyHasEmployee() {
        // given
        Station targetStation = mock(Station.class);

        StationEmployee previousOccupant = mock(StationEmployee.class);
        StationEmployee newEmployee = mock(StationEmployee.class);
        when(newEmployee.getStation()).thenReturn(null);

        when(stationEmployeeRepository.findStationEmployeeByEmployeeId(employeeId))
                .thenReturn(Optional.of(newEmployee));
        when(stationEmployeeRepository.findByStationId(stationId))
                .thenReturn(Optional.of(previousOccupant));
        when(stationService.getStation(stationId)).thenReturn(targetStation);

        // when
        stationEmployeeService.changeStation(employeeId, stationId);

        // then
        verify(previousOccupant).setStation(null);
        verify(newEmployee).setStation(targetStation);
    }

    @Test
    void removeStation_shouldClearStation_whenEmployeeExistsForStation() {
        // given
        StationEmployee employee = mock(StationEmployee.class);
        when(stationEmployeeRepository.findByStationId(stationId))
                .thenReturn(Optional.of(employee));

        // when
        stationEmployeeService.removeStation(stationId);

        // then
        verify(employee).setStation(null);
    }

    @Test
    void removeStation_shouldThrow_whenNoEmployeeAssignedToStation() {
        // given
        when(stationEmployeeRepository.findByStationId(stationId))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> stationEmployeeService.removeStation(stationId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("station not found");
    }
}