package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.station.CreateStationRequest;
import com.example.smart_fuel_management_system.dto.pump.PumpResponse;
import com.example.smart_fuel_management_system.dto.station.StationDashboardSummaryResponse;
import com.example.smart_fuel_management_system.dto.station.StationDetailsResponse;
import com.example.smart_fuel_management_system.dto.station.StationEmployeeResponse;
import com.example.smart_fuel_management_system.dto.station.StationResponse;
import com.example.smart_fuel_management_system.dto.station.StationSummaryResponse;
import com.example.smart_fuel_management_system.dto.station.StationTransactionResponse;
import com.example.smart_fuel_management_system.dto.station.StationValidationResponse;
import com.example.smart_fuel_management_system.dto.station.TransactionStationResponse;
import com.example.smart_fuel_management_system.dto.station.UpdateStationRequest;
import com.example.smart_fuel_management_system.entity.Pump;
import com.example.smart_fuel_management_system.entity.Station;
import com.example.smart_fuel_management_system.entity.StationEmployee;
import com.example.smart_fuel_management_system.enums.StationStatusType;
import com.example.smart_fuel_management_system.exceptions.BadRequestException;
import com.example.smart_fuel_management_system.repository.StationEmployeeRepository;
import com.example.smart_fuel_management_system.repository.StationRepository;
import com.example.smart_fuel_management_system.service.PumpHeartbeatService;
import com.example.smart_fuel_management_system.service.impl.client.AuthClient;
import com.example.smart_fuel_management_system.service.impl.client.TransactionClient;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationServiceImplTest {

    @Mock
    private StationRepository stationRepository;

    @Mock
    private StationEmployeeRepository stationEmployeeRepository;

    @Mock
    private TransactionClient transactionClient;

    @Mock
    private AuthClient authClient;

    @Mock
    private PumpHeartbeatService pumpHeartbeatService;

    @InjectMocks
    private StationServiceImpl stationService;

    private UUID stationId;
    private UUID employeeId;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        stationId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        baseTime = LocalDateTime.of(2026, 9, 10, 10, 0);
    }

    private Station buildStationMock() {
        Station station = mock(Station.class);
        lenient().when(station.getId()).thenReturn(stationId);
        lenient().when(station.getName()).thenReturn("Central Station");
        lenient().when(station.getCity()).thenReturn("Istanbul");
        lenient().when(station.getAddress()).thenReturn("100 Main Street");
        lenient().when(station.getContactInformation()).thenReturn("+90 555 000 0000");
        lenient().when(station.getLatitude()).thenReturn(new BigDecimal("41.0082"));
        lenient().when(station.getLongitude()).thenReturn(new BigDecimal("28.9784"));
        lenient().when(station.getStatus()).thenReturn(StationStatusType.ACTIVE);
        lenient().when(station.getCreatedAt()).thenReturn(baseTime);
        lenient().when(station.getUpdatedAt()).thenReturn(baseTime);
        return station;
    }

    @Test
    void create_shouldSaveStationWithInactiveStatus() {
        // given
        CreateStationRequest request = new CreateStationRequest(
                "Central Station",
                "Istanbul",
                "100 Main Street",
                "+90 555 000 0000",
                new BigDecimal("41.0082"),
                new BigDecimal("28.9784"));

        // when
        stationService.create(request);

        // then
        ArgumentCaptor<Station> captor = ArgumentCaptor.forClass(Station.class);
        verify(stationRepository).save(captor.capture());

        Station saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Central Station");
        assertThat(saved.getCity()).isEqualTo("Istanbul");
        assertThat(saved.getAddress()).isEqualTo("100 Main Street");
        assertThat(saved.getContactInformation()).isEqualTo("+90 555 000 0000");
        assertThat(saved.getLatitude()).isEqualByComparingTo("41.0082");
        assertThat(saved.getLongitude()).isEqualByComparingTo("28.9784");
        assertThat(saved.getStatus()).isEqualTo(StationStatusType.INACTIVE);
    }

    @Test
    void getStationDetails_shouldReturnFullResponse_whenStationExists() {
        // given
        Station station = buildStationMock();
        when(station.getEmployee()).thenReturn(null);
        when(station.getPumps()).thenReturn(List.of());

        TransactionStationResponse stats = mock(TransactionStationResponse.class);
        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(transactionClient.getTransactionsAndVehiclesCount(stationId))
                .thenReturn(Optional.of(stats));

        // when
        StationDetailsResponse result = stationService.getStationDetails(stationId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(stationId);
        assertThat(result.name()).isEqualTo("Central Station");
        assertThat(result.city()).isEqualTo("Istanbul");
        assertThat(result.address()).isEqualTo("100 Main Street");
        assertThat(result.status()).isEqualTo(StationStatusType.ACTIVE);
        assertThat(result.transactionStats()).isSameAs(stats);
        assertThat(result.stationAccount()).isNull();
        assertThat(result.pumps()).isEmpty();
    }

    @Test
    void getStationDetails_shouldEnrichPumps_whenPumpsPresent() {
        // given
        Pump pump = mock(Pump.class);
        PumpResponse enriched = mock(PumpResponse.class);

        Station station = buildStationMock();
        when(station.getEmployee()).thenReturn(null);
        when(station.getPumps()).thenReturn(List.of(pump));

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(transactionClient.getTransactionsAndVehiclesCount(stationId))
                .thenReturn(Optional.empty());
        when(pumpHeartbeatService.enrich(pump)).thenReturn(enriched);

        // when
        StationDetailsResponse result = stationService.getStationDetails(stationId);

        // then
        assertThat(result.pumps()).containsExactly(enriched);
        verify(pumpHeartbeatService).enrich(pump);
    }

    @Test
    void getStationDetails_shouldFetchEmployeeInfo_whenEmployeeAssigned() {
        // given
        StationEmployee employee = mock(StationEmployee.class);
        when(employee.getEmployeeId()).thenReturn(employeeId);

        Station station = buildStationMock();
        when(station.getEmployee()).thenReturn(employee);
        when(station.getPumps()).thenReturn(List.of());

        StationEmployeeResponse employeeResponse = mock(StationEmployeeResponse.class);
        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(transactionClient.getTransactionsAndVehiclesCount(stationId))
                .thenReturn(Optional.empty());
        when(authClient.getEmployeeInfo(employeeId))
                .thenReturn(Optional.of(employeeResponse));

        // when
        StationDetailsResponse result = stationService.getStationDetails(stationId);

        // then
        assertThat(result.stationAccount()).isSameAs(employeeResponse);
    }

    @Test
    void getStationDetails_shouldReturnNullEmployee_whenAuthClientReturnsEmpty() {
        // given
        StationEmployee employee = mock(StationEmployee.class);
        when(employee.getEmployeeId()).thenReturn(employeeId);

        Station station = buildStationMock();
        when(station.getEmployee()).thenReturn(employee);
        when(station.getPumps()).thenReturn(List.of());

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(transactionClient.getTransactionsAndVehiclesCount(stationId))
                .thenReturn(Optional.empty());
        when(authClient.getEmployeeInfo(employeeId)).thenReturn(Optional.empty());

        // when
        StationDetailsResponse result = stationService.getStationDetails(stationId);

        // then
        assertThat(result.stationAccount()).isNull();
    }

    @Test
    void getStationDetails_shouldThrow_whenStationDoesNotExist() {
        // given
        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> stationService.getStationDetails(stationId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Station not found");

        verifyNoInteractions(transactionClient, authClient, pumpHeartbeatService);
    }

    @Test
    void getStationsByIds_shouldReturnEmptyList_whenIdsNull() {
        // when
        List<StationTransactionResponse> result = stationService.getStationsByIds(null);

        // then
        assertThat(result).isEmpty();
        verify(stationRepository, never()).findStationsByIds(anyList());
    }

    @Test
    void getStationsByIds_shouldReturnEmptyList_whenIdsEmpty() {
        // when
        List<StationTransactionResponse> result = stationService.getStationsByIds(List.of());

        // then
        assertThat(result).isEmpty();
        verify(stationRepository, never()).findStationsByIds(anyList());
    }

    @Test
    void getStationsByIds_shouldDelegateToRepository_whenIdsProvided() {
        // given
        List<UUID> ids = List.of(stationId);
        StationTransactionResponse response = mock(StationTransactionResponse.class);
        when(stationRepository.findStationsByIds(ids)).thenReturn(List.of(response));

        // when
        List<StationTransactionResponse> result = stationService.getStationsByIds(ids);

        // then
        assertThat(result).containsExactly(response);
    }

    @Test
    void changeStatus_shouldThrow_whenStatusIsNull() {
        // when / then
        assertThatThrownBy(() -> stationService.changeStatus(null, employeeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Station status cannot be null");

        verifyNoInteractions(stationRepository, stationEmployeeRepository);
    }

    @Test
    void changeStatus_shouldThrow_whenEmployeeHasNoStation() {
        // given
        when(stationEmployeeRepository.findByEmployeeId(employeeId))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() ->
                stationService.changeStatus(StationStatusType.ACTIVE, employeeId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Employee does not have station");
    }

    @Test
    void changeStatus_shouldThrow_whenStationIsInMaintenance() {
        // given
        Station station = mock(Station.class);
        when(station.getStatus()).thenReturn(StationStatusType.MAINTENANCE);
        when(stationEmployeeRepository.findByEmployeeId(employeeId))
                .thenReturn(Optional.of(station));

        // when / then
        assertThatThrownBy(() ->
                stationService.changeStatus(StationStatusType.ACTIVE, employeeId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("only admin can change the status");

        verify(station, never()).setStatus(any(StationStatusType.class));
    }

    @Test
    void changeStatus_shouldSetStatus_whenAllowed() {
        // given
        Station station = mock(Station.class);
        when(station.getStatus()).thenReturn(StationStatusType.INACTIVE);
        when(stationEmployeeRepository.findByEmployeeId(employeeId))
                .thenReturn(Optional.of(station));

        // when
        stationService.changeStatus(StationStatusType.ACTIVE, employeeId);

        // then
        verify(station).setStatus(StationStatusType.ACTIVE);
    }

    @Test
    void getDetailsForTransaction_shouldReturnResponse_whenStationExists() {
        // given
        Station station = buildStationMock();
        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));

        // when
        StationTransactionResponse result = stationService.getDetailsForTransaction(stationId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(stationId);
        assertThat(result.name()).isEqualTo("Central Station");
        assertThat(result.city()).isEqualTo("Istanbul");
        assertThat(result.address()).isEqualTo("100 Main Street");
    }

    @Test
    void getDetailsForTransaction_shouldThrow_whenStationDoesNotExist() {
        // given
        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> stationService.getDetailsForTransaction(stationId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("This station does not exist");
    }

    @Test
    void getDetailsForStation_shouldReturnResponse_whenEmployeeHasStation() {
        // given
        Station station = buildStationMock();
        when(station.getPumps()).thenReturn(List.of());
        when(stationEmployeeRepository.findByEmployeeId(employeeId))
                .thenReturn(Optional.of(station));

        // when
        StationResponse result = stationService.getDetailsForStation(employeeId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(stationId);
        assertThat(result.name()).isEqualTo("Central Station");
        assertThat(result.stationStatusType()).isEqualTo(StationStatusType.ACTIVE);
        assertThat(result.pumps()).isEmpty();
    }

    @Test
    void getDetailsForStation_shouldEnrichPumps_whenPumpsPresent() {
        // given
        Pump pump = mock(Pump.class);
        PumpResponse enriched = mock(PumpResponse.class);

        Station station = buildStationMock();
        when(station.getPumps()).thenReturn(List.of(pump));
        when(stationEmployeeRepository.findByEmployeeId(employeeId))
                .thenReturn(Optional.of(station));
        when(pumpHeartbeatService.enrich(pump)).thenReturn(enriched);

        // when
        StationResponse result = stationService.getDetailsForStation(employeeId);

        // then
        assertThat(result.pumps()).containsExactly(enriched);
    }

    @Test
    void getDetailsForStation_shouldThrow_whenEmployeeHasNoStation() {
        // given
        when(stationEmployeeRepository.findByEmployeeId(employeeId))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> stationService.getDetailsForStation(employeeId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Employee does not have station");
    }

    @Test
    void validateStation_shouldReturnBothFalse_whenStationDoesNotExist() {
        // given
        when(stationRepository.existsById(stationId)).thenReturn(false);

        // when
        StationValidationResponse result = stationService.validateStation(stationId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.stationExists()).isFalse();
        assertThat(result.hasAccount()).isFalse();
        verify(stationEmployeeRepository, never()).existsByStationId(any(UUID.class));
    }

    @Test
    void validateStation_shouldReturnHasAccountTrue_whenStationHasNoEmployee() {
        // given
        when(stationRepository.existsById(stationId)).thenReturn(true);
        when(stationEmployeeRepository.existsByStationId(stationId)).thenReturn(false);

        // when
        StationValidationResponse result = stationService.validateStation(stationId);

        // then
        assertThat(result.stationExists()).isTrue();
        assertThat(result.hasAccount()).isTrue();
    }

    @Test
    void validateStation_shouldReturnHasAccountFalse_whenStationAlreadyHasEmployee() {
        // given
        when(stationRepository.existsById(stationId)).thenReturn(true);
        when(stationEmployeeRepository.existsByStationId(stationId)).thenReturn(true);

        // when
        StationValidationResponse result = stationService.validateStation(stationId);

        // then
        assertThat(result.stationExists()).isTrue();
        assertThat(result.hasAccount()).isFalse();
    }

    @Test
    void getStation_shouldReturnStation_whenExists() {
        // given
        Station station = mock(Station.class);
        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));

        // when
        Station result = stationService.getStation(stationId);

        // then
        assertThat(result).isSameAs(station);
    }

    @Test
    void getStation_shouldThrow_whenStationDoesNotExist() {
        // given
        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> stationService.getStation(stationId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("station not found");
    }

    @Test
    void getDashboardSummary_shouldAggregateCounts() {
        // given
        when(stationRepository.count()).thenReturn(10L);
        when(stationEmployeeRepository.count()).thenReturn(7L);
        when(stationRepository.countByStatus(StationStatusType.ACTIVE)).thenReturn(5L);
        when(stationRepository.countByStatus(StationStatusType.INACTIVE)).thenReturn(3L);
        when(stationRepository.countByStatus(StationStatusType.MAINTENANCE)).thenReturn(2L);

        // when
        StationDashboardSummaryResponse result = stationService.getDashboardSummary();

        // then
        assertThat(result.totalStations()).isEqualTo(10L);
        assertThat(result.totalStationAccounts()).isEqualTo(7L);
        assertThat(result.activeStations()).isEqualTo(5L);
        assertThat(result.inactiveStations()).isEqualTo(3L);
        assertThat(result.maintenanceStations()).isEqualTo(2L);
    }

    @Test
    void updateStation_shouldApplyAllFields_whenAllProvided() {
        // given
        Station station = buildStationMock();
        UpdateStationRequest request = UpdateStationRequest.builder()
                .name("Renamed")
                .address("200 New Street")
                .city("Ankara")
                .contactInformation("+90 555 111 1111")
                .latitude(new BigDecimal("39.9334"))
                .longitude(new BigDecimal("32.8597"))
                .status(StationStatusType.MAINTENANCE)
                .build();
        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));

        // when
        stationService.updateStation(stationId, request);

        // then
        verify(station).setName("Renamed");
        verify(station).setAddress("200 New Street");
        verify(station).setCity("Ankara");
        verify(station).setContactInformation("+90 555 111 1111");
        verify(station).setLatitude(new BigDecimal("39.9334"));
        verify(station).setLongitude(new BigDecimal("32.8597"));
        verify(station).setStatus(StationStatusType.MAINTENANCE);
    }

    @Test
    void updateStation_shouldSkipNullFields_whenOnlySomeProvided() {
        // given
        Station station = buildStationMock();
        UpdateStationRequest request = UpdateStationRequest.builder()
                .name("Renamed")
                .build();
        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));

        // when
        stationService.updateStation(stationId, request);

        // then
        verify(station).setName("Renamed");
        verify(station, never()).setAddress(any());
        verify(station, never()).setCity(any());
        verify(station, never()).setContactInformation(any());
        verify(station, never()).setLatitude(any());
        verify(station, never()).setLongitude(any());
        verify(station, never()).setStatus(any());
    }

    @Test
    void updateStation_shouldThrow_whenStationDoesNotExist() {
        // given
        UpdateStationRequest request = UpdateStationRequest.builder()
                .name("Renamed")
                .build();
        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> stationService.updateStation(stationId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Station not found");
    }

    @Test
    void searchAndFilterStations_shouldTrimSearch_whenSearchIsBlank() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Station> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(stationRepository.searchAndFilter(null, null, pageable)).thenReturn(emptyPage);
        when(transactionClient.getTransactionsAndVehiclesCount(List.of())).thenReturn(List.of());

        // when
        Page<StationSummaryResponse> result =
                stationService.searchAndFilterStations("   ", null, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        verify(stationRepository).searchAndFilter(null, null, pageable);
    }

    @Test
    void searchAndFilterStations_shouldTrimSearch_whenSearchHasWhitespace() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Station> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(stationRepository.searchAndFilter("Central", null, pageable)).thenReturn(emptyPage);
        when(transactionClient.getTransactionsAndVehiclesCount(List.of())).thenReturn(List.of());

        // when
        stationService.searchAndFilterStations("  Central  ", null, pageable);

        // then
        verify(stationRepository).searchAndFilter("Central", null, pageable);
    }

    @Test
    void searchAndFilterStations_shouldMapStations_whenStationsExist() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Station station = buildStationMock();
        Page<Station> page = new PageImpl<>(List.of(station), pageable, 1);

        TransactionStationResponse stats = mock(TransactionStationResponse.class);
        when(stats.stationId()).thenReturn(stationId);

        when(stationRepository.searchAndFilter(null, null, pageable)).thenReturn(page);
        when(transactionClient.getTransactionsAndVehiclesCount(List.of(stationId)))
                .thenReturn(List.of(stats));

        // when
        Page<StationSummaryResponse> result =
                stationService.searchAndFilterStations(null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        StationSummaryResponse summary = result.getContent().get(0);
        assertThat(summary.id()).isEqualTo(stationId);
        assertThat(summary.name()).isEqualTo("Central Station");
        assertThat(summary.city()).isEqualTo("Istanbul");
        assertThat(summary.address()).isEqualTo("100 Main Street");
        assertThat(summary.status()).isEqualTo(StationStatusType.ACTIVE);
        assertThat(summary.transactionStats()).isSameAs(stats);
    }

    @Test
    void searchAndFilterStations_shouldReturnNullStats_whenTransactionDataMissing() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Station station = buildStationMock();
        Page<Station> page = new PageImpl<>(List.of(station), pageable, 1);

        when(stationRepository.searchAndFilter(null, null, pageable)).thenReturn(page);
        when(transactionClient.getTransactionsAndVehiclesCount(List.of(stationId)))
                .thenReturn(List.of());

        // when
        Page<StationSummaryResponse> result =
                stationService.searchAndFilterStations(null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).transactionStats()).isNull();
    }
}