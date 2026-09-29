package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.ListResponse;
import com.example.smart_fuel_management_system.dto.VehicleDTO;
import com.example.smart_fuel_management_system.entity.Vehicle;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.enums.VehicleStatusType;
import com.example.smart_fuel_management_system.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleAdminServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleAdminServiceImpl vehicleAdminService;

    private UUID vehicleId;
    private UUID userId;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        vehicleId = UUID.randomUUID();
        userId = UUID.randomUUID();
        baseTime = LocalDateTime.of(2026, 9, 10, 10, 0);
    }

    private Vehicle buildVehicle() {
        return Vehicle.builder()
                .id(vehicleId)
                .userId(userId)
                .plateNumber("34ABC123")
                .rfidTag("RFID-1")
                .brand("Toyota")
                .model("Corolla")
                .year(2020)
                .fuelType(FuelType.GASOLINE)
                .tankCapacity(new BigDecimal("50.00"))
                .status(VehicleStatusType.ACTIVE)
                .createdAt(baseTime)
                .updatedAt(baseTime)
                .build();
    }

    // ---------------------------------------------------------------------
    // changeStatus
    // ---------------------------------------------------------------------

    @Test
    void changeStatus_shouldUpdateStatusAndSave_whenVehicleExists() {
        // given
        Vehicle vehicle = buildVehicle();
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));

        // when
        vehicleAdminService.changeStatus(vehicleId, VehicleStatusType.INACTIVE);

        // then
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatusType.INACTIVE);
        verify(vehicleRepository).save(vehicle);
    }

    @Test
    void changeStatus_shouldThrow_whenVehicleDoesNotExist() {
        // given
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() ->
                vehicleAdminService.changeStatus(vehicleId, VehicleStatusType.INACTIVE))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("This vehicle is not found");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    // ---------------------------------------------------------------------
    // delete
    // ---------------------------------------------------------------------

    @Test
    void delete_shouldRemoveVehicle_whenVehicleExists() {
        // given
        when(vehicleRepository.existsById(vehicleId)).thenReturn(true);

        // when
        vehicleAdminService.delete(vehicleId);

        // then
        verify(vehicleRepository).deleteById(vehicleId);
    }

    @Test
    void delete_shouldThrow_whenVehicleDoesNotExist() {
        // given
        when(vehicleRepository.existsById(vehicleId)).thenReturn(false);

        // when / then
        assertThatThrownBy(() -> vehicleAdminService.delete(vehicleId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("This vehicle is not found");

        verify(vehicleRepository, never()).deleteById(any(UUID.class));
    }

    // ---------------------------------------------------------------------
    // findByPlateNumber
    // ---------------------------------------------------------------------

    @Test
    void findByPlateNumber_shouldReturnVehicleDTO_whenVehicleExists() {
        // given
        Vehicle vehicle = buildVehicle();
        when(vehicleRepository.findByPlateNumber("34ABC123"))
                .thenReturn(Optional.of(vehicle));

        // when
        VehicleDTO result = vehicleAdminService.findByPlateNumber("34ABC123");

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(vehicleId);
        assertThat(result.plateNumber()).isEqualTo("34ABC123");
        assertThat(result.rfidTag()).isEqualTo("RFID-1");
        assertThat(result.brand()).isEqualTo("Toyota");
        assertThat(result.model()).isEqualTo("Corolla");
        assertThat(result.year()).isEqualTo(2020);
        assertThat(result.fuelType()).isEqualTo(FuelType.GASOLINE);
        assertThat(result.status()).isEqualTo(VehicleStatusType.ACTIVE);
        assertThat(result.createdAt()).isEqualTo(baseTime);
        assertThat(result.updatedAt()).isEqualTo(baseTime);
    }

    @Test
    void findByPlateNumber_shouldThrow_whenVehicleDoesNotExist() {
        // given
        when(vehicleRepository.findByPlateNumber("00NONE00"))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() ->
                vehicleAdminService.findByPlateNumber("00NONE00"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("This vehicle is not found");
    }

    // ---------------------------------------------------------------------
    // getUserVehicles
    // ---------------------------------------------------------------------

    @Test
    void getUserVehicles_shouldReturnMappedList_whenUserHasVehicles() {
        // given
        Vehicle vehicle = buildVehicle();
        when(vehicleRepository.findByUserId(userId)).thenReturn(List.of(vehicle));

        // when
        List<VehicleDTO> result = vehicleAdminService.getUserVehicles(userId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(vehicleId);
        assertThat(result.get(0).plateNumber()).isEqualTo("34ABC123");
    }

    @Test
    void getUserVehicles_shouldReturnEmptyList_whenUserHasNoVehicles() {
        // given
        when(vehicleRepository.findByUserId(userId)).thenReturn(List.of());

        // when
        List<VehicleDTO> result = vehicleAdminService.getUserVehicles(userId);

        // then
        assertThat(result).isEmpty();
    }

    // ---------------------------------------------------------------------
    // getVehicles
    // ---------------------------------------------------------------------

    @Test
    void getVehicles_shouldReturnPagedListResponses_whenVehiclesExist() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Vehicle vehicle = buildVehicle();
        Page<Vehicle> page = new PageImpl<>(List.of(vehicle), pageable, 1);
        when(vehicleRepository.findAll(pageable)).thenReturn(page);

        // when
        Page<ListResponse> result = vehicleAdminService.getVehicles(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        ListResponse response = result.getContent().get(0);
        assertThat(response.id()).isEqualTo(vehicleId);
        assertThat(response.brand()).isEqualTo("Toyota");
        assertThat(response.model()).isEqualTo("Corolla");
        assertThat(response.year()).isEqualTo(2020);
        assertThat(response.status()).isEqualTo(VehicleStatusType.ACTIVE);
    }

    @Test
    void getVehicles_shouldReturnEmptyPage_whenNoVehiclesExist() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        when(vehicleRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        // when
        Page<ListResponse> result = vehicleAdminService.getVehicles(pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    // ---------------------------------------------------------------------
    // getVehicleDetails
    // ---------------------------------------------------------------------

    @Test
    void getVehicleDetails_shouldReturnVehicleDTO_whenVehicleExists() {
        // given
        Vehicle vehicle = buildVehicle();
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));

        // when
        VehicleDTO result = vehicleAdminService.getVehicleDetails(vehicleId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(vehicleId);
        assertThat(result.plateNumber()).isEqualTo("34ABC123");
        assertThat(result.rfidTag()).isEqualTo("RFID-1");
        assertThat(result.brand()).isEqualTo("Toyota");
        assertThat(result.model()).isEqualTo("Corolla");
        assertThat(result.year()).isEqualTo(2020);
        assertThat(result.fuelType()).isEqualTo(FuelType.GASOLINE);
        assertThat(result.status()).isEqualTo(VehicleStatusType.ACTIVE);
        assertThat(result.createdAt()).isEqualTo(baseTime);
        assertThat(result.updatedAt()).isEqualTo(baseTime);
    }

    @Test
    void getVehicleDetails_shouldThrow_whenVehicleDoesNotExist() {
        // given
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> vehicleAdminService.getVehicleDetails(vehicleId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Vehicle not found: " + vehicleId);
    }
}