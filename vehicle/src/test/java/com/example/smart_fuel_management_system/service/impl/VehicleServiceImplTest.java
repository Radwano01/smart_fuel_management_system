package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.AddRequest;
import com.example.smart_fuel_management_system.dto.ListResponse;
import com.example.smart_fuel_management_system.dto.RecentVehicleResponse;
import com.example.smart_fuel_management_system.dto.UpdateRequest;
import com.example.smart_fuel_management_system.dto.VehicleCountResponse;
import com.example.smart_fuel_management_system.dto.VehicleDTO;
import com.example.smart_fuel_management_system.dto.VehicleDashboardSummaryResponse;
import com.example.smart_fuel_management_system.dto.VehicleResponse;
import com.example.smart_fuel_management_system.dto.VehicleStationResponse;
import com.example.smart_fuel_management_system.dto.VehicleTransactionResponse;
import com.example.smart_fuel_management_system.entity.Vehicle;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.enums.VehicleStatusType;
import com.example.smart_fuel_management_system.exception.BadRequestException;
import com.example.smart_fuel_management_system.repository.VehicleRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleSpecService vehicleSpecService;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private UUID userId;
    private UUID vehicleId;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        vehicleId = UUID.randomUUID();
        baseTime = LocalDateTime.of(2026, 9, 10, 10, 0);
    }

    private Vehicle buildVehicle(VehicleStatusType status, String rfidTag) {
        Vehicle vehicle = new Vehicle(
                "34ABC123",
                "Toyota",
                "Corolla",
                2020,
                new BigDecimal("50.00"),
                FuelType.GASOLINE,
                rfidTag,
                status,
                userId
        );
        vehicle.setId(vehicleId);
        vehicle.setCreatedAt(baseTime);
        vehicle.setUpdatedAt(baseTime);
        return vehicle;
    }

    // ---------------------------------------------------------------------
    // create
    // ---------------------------------------------------------------------

    @Test
    void create_shouldThrow_whenPlateNumberAlreadyExists() {
        // given
        AddRequest request = new AddRequest(
                "34ABC123", "Toyota", "Corolla", 2020, FuelType.GASOLINE);
        when(vehicleRepository.existsByPlateNumber("34ABC123")).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> vehicleService.create(userId, request))
                .isInstanceOf(EntityExistsException.class)
                .hasMessageContaining("plate number is already exist");

        verify(vehicleSpecService, never()).getTankCapacity(anyString(), anyString(), anyInt(), any());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void create_shouldSaveVehicle_whenPlateIsUnique() {
        // given
        AddRequest request = new AddRequest(
                "34ABC123", "Toyota", "Corolla", 2020, FuelType.GASOLINE);
        when(vehicleRepository.existsByPlateNumber("34ABC123")).thenReturn(false);
        when(vehicleSpecService.getTankCapacity(
                "Toyota", "Corolla", 2020, FuelType.GASOLINE))
                .thenReturn(new BigDecimal("50.00"));

        // when
        vehicleService.create(userId, request);

        // then
        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(captor.capture());

        Vehicle saved = captor.getValue();
        assertThat(saved.getPlateNumber()).isEqualTo("34ABC123");
        assertThat(saved.getBrand()).isEqualTo("Toyota");
        assertThat(saved.getModel()).isEqualTo("Corolla");
        assertThat(saved.getYear()).isEqualTo(2020);
        assertThat(saved.getTankCapacity()).isEqualByComparingTo("50.00");
        assertThat(saved.getFuelType()).isEqualTo(FuelType.GASOLINE);
        assertThat(saved.getRfidTag()).isNull();
        assertThat(saved.getStatus()).isEqualTo(VehicleStatusType.PENDING);
        assertThat(saved.getUserId()).isEqualTo(userId);
    }

    // ---------------------------------------------------------------------
    // update
    // ---------------------------------------------------------------------

    @Test
    void update_shouldThrow_whenUserDoesNotOwnVehicle() {
        // given
        UpdateRequest request = new UpdateRequest("Honda", "Civic", FuelType.DIESEL);
        when(vehicleRepository.findByUserIdAndId(userId, vehicleId))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> vehicleService.update(userId, vehicleId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("This user does not own this vehicle");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void update_shouldApplyAllFields_whenAllProvided() {
        // given
        Vehicle vehicle = buildVehicle(VehicleStatusType.ACTIVE, "RFID-1");
        UpdateRequest request = new UpdateRequest("Honda", "Civic", FuelType.DIESEL);
        when(vehicleRepository.findByUserIdAndId(userId, vehicleId))
                .thenReturn(Optional.of(vehicle));

        // when
        vehicleService.update(userId, vehicleId, request);

        // then
        assertThat(vehicle.getBrand()).isEqualTo("Honda");
        assertThat(vehicle.getModel()).isEqualTo("Civic");
        assertThat(vehicle.getFuelType()).isEqualTo(FuelType.DIESEL);
        verify(vehicleRepository).save(vehicle);
    }

    @Test
    void update_shouldSkipBlankFields_whenTheyAreNull() {
        // given
        Vehicle vehicle = buildVehicle(VehicleStatusType.ACTIVE, "RFID-1");
        UpdateRequest request = new UpdateRequest(null, null, null);
        when(vehicleRepository.findByUserIdAndId(userId, vehicleId))
                .thenReturn(Optional.of(vehicle));

        // when
        vehicleService.update(userId, vehicleId, request);

        // then
        assertThat(vehicle.getBrand()).isEqualTo("Toyota");
        assertThat(vehicle.getModel()).isEqualTo("Corolla");
        assertThat(vehicle.getFuelType()).isEqualTo(FuelType.GASOLINE);
        verify(vehicleRepository).save(vehicle);
    }

    @Test
    void update_shouldSkipBlankFields_whenTheyAreWhitespace() {
        // given
        Vehicle vehicle = buildVehicle(VehicleStatusType.ACTIVE, "RFID-1");
        UpdateRequest request = new UpdateRequest("   ", "   ", null);
        when(vehicleRepository.findByUserIdAndId(userId, vehicleId))
                .thenReturn(Optional.of(vehicle));

        // when
        vehicleService.update(userId, vehicleId, request);

        // then
        assertThat(vehicle.getBrand()).isEqualTo("Toyota");
        assertThat(vehicle.getModel()).isEqualTo("Corolla");
    }

    // ---------------------------------------------------------------------
    // deactivate
    // ---------------------------------------------------------------------

    @Test
    void deactivate_shouldThrow_whenUserDoesNotOwnVehicle() {
        // given
        when(vehicleRepository.findByUserIdAndId(userId, vehicleId))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> vehicleService.deactivate(userId, vehicleId))
                .isInstanceOf(EntityNotFoundException.class);

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void deactivate_shouldThrow_whenVehicleIsPending() {
        // given
        Vehicle vehicle = buildVehicle(VehicleStatusType.PENDING, null);
        when(vehicleRepository.findByUserIdAndId(userId, vehicleId))
                .thenReturn(Optional.of(vehicle));

        // when / then
        assertThatThrownBy(() -> vehicleService.deactivate(userId, vehicleId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot move from PENDING");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void deactivate_shouldSetInactive_whenVehicleIsActive() {
        // given
        Vehicle vehicle = buildVehicle(VehicleStatusType.ACTIVE, null);
        when(vehicleRepository.findByUserIdAndId(userId, vehicleId))
                .thenReturn(Optional.of(vehicle));

        // when
        vehicleService.deactivate(userId, vehicleId);

        // then
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatusType.INACTIVE);
        verify(vehicleRepository).save(vehicle);
    }

    // ---------------------------------------------------------------------
    // activate
    // ---------------------------------------------------------------------

    @Test
    void activate_shouldThrow_whenUserDoesNotOwnVehicle() {
        // given
        when(vehicleRepository.findByUserIdAndId(userId, vehicleId))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> vehicleService.activate(userId, vehicleId))
                .isInstanceOf(EntityNotFoundException.class);

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void activate_shouldThrow_whenVehicleIsPending() {
        // given
        Vehicle vehicle = buildVehicle(VehicleStatusType.PENDING, null);
        when(vehicleRepository.findByUserIdAndId(userId, vehicleId))
                .thenReturn(Optional.of(vehicle));

        // when / then
        assertThatThrownBy(() -> vehicleService.activate(userId, vehicleId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot move from PENDING");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void activate_shouldSetActive_whenVehicleIsInactive() {
        // given
        Vehicle vehicle = buildVehicle(VehicleStatusType.INACTIVE, null);
        when(vehicleRepository.findByUserIdAndId(userId, vehicleId))
                .thenReturn(Optional.of(vehicle));

        // when
        vehicleService.activate(userId, vehicleId);

        // then
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatusType.ACTIVE);
        verify(vehicleRepository).save(vehicle);
    }

    // ---------------------------------------------------------------------
    // findAllByUserId
    // ---------------------------------------------------------------------

    @Test
    void findAllByUserId_shouldReturnMappedList_whenUserHasVehicles() {
        // given
        Vehicle vehicle = buildVehicle(VehicleStatusType.ACTIVE, null);
        when(vehicleRepository.findAllByUserId(userId)).thenReturn(List.of(vehicle));

        // when
        List<ListResponse> result = vehicleService.findAllByUserId(userId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(vehicleId);
        assertThat(result.get(0).brand()).isEqualTo("Toyota");
        assertThat(result.get(0).model()).isEqualTo("Corolla");
        assertThat(result.get(0).year()).isEqualTo(2020);
        assertThat(result.get(0).status()).isEqualTo(VehicleStatusType.ACTIVE);
    }

    @Test
    void findAllByUserId_shouldReturnEmptyList_whenUserHasNoVehicles() {
        // given
        when(vehicleRepository.findAllByUserId(userId)).thenReturn(List.of());

        // when
        List<ListResponse> result = vehicleService.findAllByUserId(userId);

        // then
        assertThat(result).isEmpty();
    }

    // ---------------------------------------------------------------------
    // findByUserIdAndVehicleId
    // ---------------------------------------------------------------------

    @Test
    void findByUserIdAndVehicleId_shouldReturnDTO_whenOwnedByUser() {
        // given
        Vehicle vehicle = buildVehicle(VehicleStatusType.ACTIVE, "RFID-1");
        when(vehicleRepository.findByUserIdAndId(userId, vehicleId))
                .thenReturn(Optional.of(vehicle));

        // when
        VehicleDTO result = vehicleService.findByUserIdAndVehicleId(userId, vehicleId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(vehicleId);
        assertThat(result.plateNumber()).isEqualTo("34ABC123");
        assertThat(result.rfidTag()).isEqualTo("RFID-1");
        assertThat(result.status()).isEqualTo(VehicleStatusType.ACTIVE);
    }

    @Test
    void findByUserIdAndVehicleId_shouldThrow_whenNotFound() {
        // given
        when(vehicleRepository.findByUserIdAndId(userId, vehicleId))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() ->
                vehicleService.findByUserIdAndVehicleId(userId, vehicleId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("This vehicle is not found");
    }

    // ---------------------------------------------------------------------
    // resolve
    // ---------------------------------------------------------------------

    @Test
    void resolve_shouldReturnResponse_whenVehicleExists() {
        // given
        Vehicle vehicle = buildVehicle(VehicleStatusType.ACTIVE, "RFID-1");
        when(vehicleRepository.findByRfidTagAndPlateNumber("RFID-1", "34ABC123"))
                .thenReturn(Optional.of(vehicle));

        // when
        VehicleResponse result = vehicleService.resolve("RFID-1", "34ABC123");

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.vehicleId()).isEqualTo(vehicleId);
        assertThat(result.fuelType()).isEqualTo(FuelType.GASOLINE);
        assertThat(result.tankCapacity()).isEqualByComparingTo("50.00");
        assertThat(result.vehicleStatusType()).isEqualTo(VehicleStatusType.ACTIVE);
    }

    @Test
    void resolve_shouldThrow_whenVehicleNotFound() {
        // given
        when(vehicleRepository.findByRfidTagAndPlateNumber("RFID-NONE", "34ABC123"))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> vehicleService.resolve("RFID-NONE", "34ABC123"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Vehicle Not found");
    }

    // ---------------------------------------------------------------------
    // getVehicleCount
    // ---------------------------------------------------------------------

    @Test
    void getVehicleCount_shouldReturnCountFromRepository() {
        // given
        when(vehicleRepository.countByUserId(userId)).thenReturn(3L);

        // when
        VehicleCountResponse result = vehicleService.getVehicleCount(userId);

        // then
        assertThat(result.count()).isEqualTo(3L);
    }

    // ---------------------------------------------------------------------
    // getVehicleSummaryDetails
    // ---------------------------------------------------------------------

    @Test
    void getVehicleSummaryDetails_shouldReturnResponse_whenVehicleExists() {
        // given
        VehicleTransactionResponse expected = mock(VehicleTransactionResponse.class);
        when(vehicleRepository.findVehicleForTransaction(vehicleId))
                .thenReturn(Optional.of(expected));

        // when
        VehicleTransactionResponse result =
                vehicleService.getVehicleSummaryDetails(vehicleId);

        // then
        assertThat(result).isSameAs(expected);
    }

    @Test
    void getVehicleSummaryDetails_shouldThrow_whenVehicleNotFound() {
        // given
        when(vehicleRepository.findVehicleForTransaction(vehicleId))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> vehicleService.getVehicleSummaryDetails(vehicleId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("This car is not found");
    }

    // ---------------------------------------------------------------------
    // getDashboardSummary
    // ---------------------------------------------------------------------

    @Test
    void getDashboardSummary_shouldAggregateCountsAndRecentVehicles() {
        // given
        Vehicle vehicle = buildVehicle(VehicleStatusType.ACTIVE, null);
        when(vehicleRepository.findTop10ByOrderByCreatedAtDesc())
                .thenReturn(List.of(vehicle));
        when(vehicleRepository.count()).thenReturn(10L);
        when(vehicleRepository.countByStatus(VehicleStatusType.ACTIVE)).thenReturn(7L);

        // when
        VehicleDashboardSummaryResponse result = vehicleService.getDashboardSummary();

        // then
        assertThat(result.totalVehicles()).isEqualTo(10L);
        assertThat(result.activeVehicles()).isEqualTo(7L);
        assertThat(result.recentVehicles()).hasSize(1);
        RecentVehicleResponse recent = result.recentVehicles().get(0);
        assertThat(recent.id()).isEqualTo(vehicleId);
        assertThat(recent.plateNumber()).isEqualTo("34ABC123");
        assertThat(recent.brand()).isEqualTo("Toyota");
        assertThat(recent.model()).isEqualTo("Corolla");
        assertThat(recent.createdAt()).isEqualTo(baseTime);
    }

    @Test
    void getDashboardSummary_shouldReturnEmptyRecentList_whenNoVehiclesExist() {
        // given
        when(vehicleRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(List.of());
        when(vehicleRepository.count()).thenReturn(0L);
        when(vehicleRepository.countByStatus(VehicleStatusType.ACTIVE)).thenReturn(0L);

        // when
        VehicleDashboardSummaryResponse result = vehicleService.getDashboardSummary();

        // then
        assertThat(result.totalVehicles()).isZero();
        assertThat(result.activeVehicles()).isZero();
        assertThat(result.recentVehicles()).isEmpty();
    }

    // ---------------------------------------------------------------------
    // getVehiclesByIds
    // ---------------------------------------------------------------------

    @Test
    void getVehiclesByIds_shouldReturnEmptyList_whenIdsNull() {
        // when
        List<VehicleTransactionResponse> result = vehicleService.getVehiclesByIds(null);

        // then
        assertThat(result).isEmpty();
        verify(vehicleRepository, never()).findVehiclesByIds(any());
    }

    @Test
    void getVehiclesByIds_shouldReturnEmptyList_whenIdsEmpty() {
        // when
        List<VehicleTransactionResponse> result =
                vehicleService.getVehiclesByIds(List.of());

        // then
        assertThat(result).isEmpty();
        verify(vehicleRepository, never()).findVehiclesByIds(any());
    }

    @Test
    void getVehiclesByIds_shouldDelegateToRepository_whenIdsProvided() {
        // given
        List<UUID> ids = List.of(vehicleId, UUID.randomUUID());
        VehicleTransactionResponse response = mock(VehicleTransactionResponse.class);
        when(vehicleRepository.findVehiclesByIds(ids)).thenReturn(List.of(response));

        // when
        List<VehicleTransactionResponse> result = vehicleService.getVehiclesByIds(ids);

        // then
        assertThat(result).containsExactly(response);
        verify(vehicleRepository).findVehiclesByIds(ids);
    }

    // ---------------------------------------------------------------------
    // getVehicleByPlateNumber
    // ---------------------------------------------------------------------

    @Test
    void getVehicleByPlateNumber_shouldReturnStationResponse_whenVehicleExists() {
        // given
        Vehicle vehicle = buildVehicle(VehicleStatusType.ACTIVE, "RFID-1");
        when(vehicleRepository.findByPlateNumber("34ABC123"))
                .thenReturn(Optional.of(vehicle));

        // when
        VehicleStationResponse result =
                vehicleService.getVehicleByPlateNumber("34ABC123");

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(vehicleId);
        assertThat(result.plateNumber()).isEqualTo("34ABC123");
        assertThat(result.rfidTag()).isEqualTo("RFID-1");
        assertThat(result.brand()).isEqualTo("Toyota");
        assertThat(result.model()).isEqualTo("Corolla");
        assertThat(result.year()).isEqualTo(2020);
        assertThat(result.status()).isEqualTo(VehicleStatusType.ACTIVE);
    }

    @Test
    void getVehicleByPlateNumber_shouldThrow_whenVehicleNotFound() {
        // given
        when(vehicleRepository.findByPlateNumber("00NONE00"))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> vehicleService.getVehicleByPlateNumber("00NONE00"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("vehicle not found");
    }
}