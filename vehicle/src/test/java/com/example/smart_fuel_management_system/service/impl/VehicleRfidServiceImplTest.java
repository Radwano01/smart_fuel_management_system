package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.VehicleDTO;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleRfidServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleRfidServiceImpl vehicleRfidService;

    private UUID vehicleId;
    private UUID userId;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        vehicleId = UUID.randomUUID();
        userId = UUID.randomUUID();
        baseTime = LocalDateTime.of(2026, 9, 10, 10, 0);
    }

    private Vehicle buildVehicle(String rfidTag) {
        return Vehicle.builder()
                .id(vehicleId)
                .userId(userId)
                .plateNumber("34ABC123")
                .rfidTag(rfidTag)
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
    // assignRfid
    // ---------------------------------------------------------------------

    @Test
    void assignRfid_shouldThrow_whenVehicleDoesNotExist() {
        // given
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() ->
                vehicleRfidService.assignRfid(vehicleId, "RFID-NEW"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Vehicle not found");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void assignRfid_shouldThrow_whenRfidIsNull() {
        // given
        Vehicle vehicle = buildVehicle(null);
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));

        // when / then
        assertThatThrownBy(() ->
                vehicleRfidService.assignRfid(vehicleId, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("RFID cannot be empty");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void assignRfid_shouldThrow_whenRfidIsBlank() {
        // given
        Vehicle vehicle = buildVehicle(null);
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));

        // when / then
        assertThatThrownBy(() ->
                vehicleRfidService.assignRfid(vehicleId, "   "))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("RFID cannot be empty");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void assignRfid_shouldThrow_whenVehicleAlreadyHasSameRfid() {
        // given
        Vehicle vehicle = buildVehicle("RFID-1");
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));

        // when / then
        assertThatThrownBy(() ->
                vehicleRfidService.assignRfid(vehicleId, "RFID-1"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("RFID is already assigned to this vehicle");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void assignRfid_shouldThrow_whenVehicleAlreadyHasDifferentRfid() {
        // given
        Vehicle vehicle = buildVehicle("RFID-1");
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));

        // when / then
        assertThatThrownBy(() ->
                vehicleRfidService.assignRfid(vehicleId, "RFID-2"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Vehicle already has an RFID assigned");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void assignRfid_shouldThrow_whenRfidBelongsToAnotherVehicle() {
        // given
        Vehicle vehicle = buildVehicle(null);
        Vehicle otherVehicle = buildVehicle("RFID-2");
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.findByRfidTag("RFID-2")).thenReturn(Optional.of(otherVehicle));

        // when / then
        assertThatThrownBy(() ->
                vehicleRfidService.assignRfid(vehicleId, "RFID-2"))
                .isInstanceOf(EntityExistsException.class)
                .hasMessageContaining("RFID already assigned to another vehicle");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void assignRfid_shouldSetRfidAndSave_whenVehicleHasNoRfidAndRfidIsFree() {
        // given
        Vehicle vehicle = buildVehicle(null);
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.findByRfidTag("RFID-NEW")).thenReturn(Optional.empty());

        // when
        vehicleRfidService.assignRfid(vehicleId, "RFID-NEW");

        // then
        assertThat(vehicle.getRfidTag()).isEqualTo("RFID-NEW");
        verify(vehicleRepository).save(vehicle);
    }

    // ---------------------------------------------------------------------
    // findByRfid
    // ---------------------------------------------------------------------

    @Test
    void findByRfid_shouldThrow_whenRfidIsNull() {
        // when / then
        assertThatThrownBy(() -> vehicleRfidService.findByRfid(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("RFID cannot be empty");

        verify(vehicleRepository, never()).findByRfidTag(any(String.class));
    }

    @Test
    void findByRfid_shouldThrow_whenRfidIsBlank() {
        // when / then
        assertThatThrownBy(() -> vehicleRfidService.findByRfid("   "))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("RFID cannot be empty");

        verify(vehicleRepository, never()).findByRfidTag(any(String.class));
    }

    @Test
    void findByRfid_shouldThrow_whenNoVehicleHasRfid() {
        // given
        when(vehicleRepository.findByRfidTag("RFID-NONE")).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> vehicleRfidService.findByRfid("RFID-NONE"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Vehicle not found with RFID: RFID-NONE");
    }

    @Test
    void findByRfid_shouldReturnVehicleDTO_whenVehicleExists() {
        // given
        Vehicle vehicle = buildVehicle("RFID-1");
        when(vehicleRepository.findByRfidTag("RFID-1")).thenReturn(Optional.of(vehicle));

        // when
        VehicleDTO result = vehicleRfidService.findByRfid("RFID-1");

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

    // ---------------------------------------------------------------------
    // removeRfid
    // ---------------------------------------------------------------------

    @Test
    void removeRfid_shouldThrow_whenVehicleDoesNotExist() {
        // given
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> vehicleRfidService.removeRfid(vehicleId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Vehicle not found");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void removeRfid_shouldThrow_whenVehicleHasNoRfid() {
        // given
        Vehicle vehicle = buildVehicle(null);
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));

        // when / then
        assertThatThrownBy(() -> vehicleRfidService.removeRfid(vehicleId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Vehicle has no RFID assigned");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void removeRfid_shouldClearRfidAndSave_whenVehicleHasRfid() {
        // given
        Vehicle vehicle = buildVehicle("RFID-1");
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));

        // when
        vehicleRfidService.removeRfid(vehicleId);

        // then
        assertThat(vehicle.getRfidTag()).isNull();
        verify(vehicleRepository).save(vehicle);
    }
}