package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.pump.PumpDeviceResponse;
import com.example.smart_fuel_management_system.entity.Pump;
import com.example.smart_fuel_management_system.entity.PumpDevice;
import com.example.smart_fuel_management_system.enums.DeviceStatusType;
import com.example.smart_fuel_management_system.repository.PumpDeviceRepository;
import com.example.smart_fuel_management_system.repository.PumpRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class PumpDeviceServiceImplTest {

    @Mock
    private PumpDeviceRepository pumpDeviceRepository;

    @Mock
    private PumpRepository pumpRepository;

    @InjectMocks
    private PumpDeviceServiceImpl pumpDeviceService;

    private UUID pumpId;
    private UUID deviceUuid;

    @BeforeEach
    void setUp() {
        pumpId = UUID.randomUUID();
        deviceUuid = UUID.randomUUID();
    }

    @Test
    void registerDevice_shouldThrow_whenDeviceIdIsNull() {
        // when / then
        assertThatThrownBy(() -> pumpDeviceService.registerDevice(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Device ID is required");

        verify(pumpDeviceRepository, never()).save(any(PumpDevice.class));
    }

    @Test
    void registerDevice_shouldThrow_whenDeviceIdIsBlank() {
        // when / then
        assertThatThrownBy(() -> pumpDeviceService.registerDevice("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Device ID is required");

        verify(pumpDeviceRepository, never()).save(any(PumpDevice.class));
    }

    @Test
    void registerDevice_shouldReturnNullPumpId_whenNewDeviceIsCreated() {
        // given
        String deviceId = "DEVICE-001";

        when(pumpDeviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.empty());
        when(pumpDeviceRepository.save(any(PumpDevice.class)))
                .thenAnswer(invocation -> {
                    PumpDevice input = invocation.getArgument(0);
                    PumpDevice persisted = mock(PumpDevice.class);
                    when(persisted.getId()).thenReturn(deviceUuid);
                    when(persisted.getDeviceId()).thenReturn(input.getDeviceId());
                    when(persisted.getStatus()).thenReturn(input.getStatus());
                    when(persisted.getPump()).thenReturn(null);
                    return persisted;
                });

        // when
        UUID result = pumpDeviceService.registerDevice(deviceId);

        // then
        assertThat(result).isNull();
        verify(pumpDeviceRepository).save(any(PumpDevice.class));
    }

    @Test
    void registerDevice_shouldTrimDeviceId_whenLookingUpAndSaving() {
        // given
        String padded = "  DEVICE-001  ";
        String trimmed = "DEVICE-001";

        when(pumpDeviceRepository.findByDeviceId(trimmed)).thenReturn(Optional.empty());
        when(pumpDeviceRepository.save(any(PumpDevice.class)))
                .thenAnswer(invocation -> {
                    PumpDevice input = invocation.getArgument(0);
                    PumpDevice persisted = mock(PumpDevice.class);
                    when(persisted.getId()).thenReturn(deviceUuid);
                    when(persisted.getDeviceId()).thenReturn(input.getDeviceId());
                    when(persisted.getStatus()).thenReturn(input.getStatus());
                    when(persisted.getPump()).thenReturn(null);
                    return persisted;
                });

        // when
        pumpDeviceService.registerDevice(padded);

        // then
        verify(pumpDeviceRepository).findByDeviceId(trimmed);
    }

    @Test
    void registerDevice_shouldNotCreateNew_whenDeviceAlreadyExists() {
        // given
        String deviceId = "DEVICE-001";
        PumpDevice existing = mock(PumpDevice.class);
        when(existing.getId()).thenReturn(deviceUuid);
        when(existing.getDeviceId()).thenReturn(deviceId);
        when(existing.getStatus()).thenReturn(DeviceStatusType.UNASSIGNED);
        when(existing.getPump()).thenReturn(null);

        when(pumpDeviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(existing));

        // when
        pumpDeviceService.registerDevice(deviceId);

        // then
        verify(pumpDeviceRepository, never()).save(any(PumpDevice.class));
    }

    @Test
    void registerDevice_shouldReturnPumpId_whenDeviceIsAlreadyAssigned() {
        // given
        String deviceId = "DEVICE-001";
        Pump assignedPump = mock(Pump.class);
        when(assignedPump.getId()).thenReturn(pumpId);

        PumpDevice existing = mock(PumpDevice.class);
        when(existing.getId()).thenReturn(deviceUuid);
        when(existing.getDeviceId()).thenReturn(deviceId);
        when(existing.getStatus()).thenReturn(DeviceStatusType.ASSIGNED);
        when(existing.getPump()).thenReturn(assignedPump);

        when(pumpDeviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(existing));

        // when
        UUID result = pumpDeviceService.registerDevice(deviceId);

        // then
        assertThat(result).isEqualTo(pumpId);
    }

    @Test
    void getByDeviceId_shouldReturnResponse_whenDeviceExists() {
        // given
        String deviceId = "DEVICE-001";
        PumpDevice device = mock(PumpDevice.class);
        when(device.getId()).thenReturn(deviceUuid);
        when(device.getDeviceId()).thenReturn(deviceId);
        when(device.getStatus()).thenReturn(DeviceStatusType.UNASSIGNED);
        when(device.getPump()).thenReturn(null);

        when(pumpDeviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(device));

        // when
        PumpDeviceResponse result = pumpDeviceService.getByDeviceId(deviceId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(deviceUuid);
        assertThat(result.deviceId()).isEqualTo(deviceId);
        assertThat(result.status()).isEqualTo(DeviceStatusType.UNASSIGNED);
        assertThat(result.pumpId()).isNull();
    }

    @Test
    void getByDeviceId_shouldTrimDeviceId_whenLookingUp() {
        // given
        String padded = "  DEVICE-001  ";
        String trimmed = "DEVICE-001";

        when(pumpDeviceRepository.findByDeviceId(trimmed)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> pumpDeviceService.getByDeviceId(padded))
                .isInstanceOf(EntityNotFoundException.class);

        verify(pumpDeviceRepository).findByDeviceId(trimmed);
    }

    @Test
    void getByDeviceId_shouldThrow_whenDeviceNotFound() {
        // given
        String deviceId = "DEVICE-NONE";
        when(pumpDeviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> pumpDeviceService.getByDeviceId(deviceId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Pump device not found: " + deviceId);
    }

    @Test
    void getByDeviceId_shouldReturnResponse_whenDeviceIsAssigned() {
        // given
        String deviceId = "DEVICE-001";
        Pump assignedPump = mock(Pump.class);
        when(assignedPump.getId()).thenReturn(pumpId);

        PumpDevice device = mock(PumpDevice.class);
        when(device.getId()).thenReturn(deviceUuid);
        when(device.getDeviceId()).thenReturn(deviceId);
        when(device.getStatus()).thenReturn(DeviceStatusType.ASSIGNED);
        when(device.getPump()).thenReturn(assignedPump);

        when(pumpDeviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(device));

        // when
        PumpDeviceResponse result = pumpDeviceService.getByDeviceId(deviceId);

        // then
        assertThat(result.pumpId()).isEqualTo(pumpId);
        assertThat(result.status()).isEqualTo(DeviceStatusType.ASSIGNED);
    }


    @Test
    void assignToPump_shouldThrow_whenPumpNotFound() {
        // given
        when(pumpRepository.findById(pumpId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> pumpDeviceService.assignToPump(pumpId, "DEVICE-001"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Pump not found: " + pumpId);

        verify(pumpDeviceRepository, never()).save(any(PumpDevice.class));
    }

    @Test
    void assignToPump_shouldThrow_whenDeviceNotFound() {
        // given
        Pump pump = mock(Pump.class);
        when(pumpRepository.findById(pumpId)).thenReturn(Optional.of(pump));
        when(pumpDeviceRepository.findByDeviceId("DEVICE-001")).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> pumpDeviceService.assignToPump(pumpId, "DEVICE-001"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Device not found: DEVICE-001");

        verify(pumpDeviceRepository, never()).save(any(PumpDevice.class));
    }

    @Test
    void assignToPump_shouldThrow_whenDeviceAlreadyAssignedToAnotherPump() {
        // given
        Pump pump = mock(Pump.class);
        Pump anotherPump = mock(Pump.class);

        PumpDevice device = mock(PumpDevice.class);
        when(device.getPump()).thenReturn(anotherPump);

        when(pumpRepository.findById(pumpId)).thenReturn(Optional.of(pump));
        when(pumpDeviceRepository.findByDeviceId("DEVICE-001")).thenReturn(Optional.of(device));

        // when / then
        assertThatThrownBy(() -> pumpDeviceService.assignToPump(pumpId, "DEVICE-001"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Device is already assigned to another pump");

        verify(pumpDeviceRepository, never()).save(any(PumpDevice.class));
    }

    @Test
    void assignToPump_shouldThrow_whenPumpAlreadyHasAssignedDevice() {
        // given
        Pump pump = mock(Pump.class);

        PumpDevice device = mock(PumpDevice.class);
        when(device.getPump()).thenReturn(null);

        when(pumpRepository.findById(pumpId)).thenReturn(Optional.of(pump));
        when(pumpDeviceRepository.findByDeviceId("DEVICE-001")).thenReturn(Optional.of(device));
        when(pumpDeviceRepository.existsByPump_Id(pumpId)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> pumpDeviceService.assignToPump(pumpId, "DEVICE-001"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Pump already has an assigned device");

        verify(pumpDeviceRepository, never()).save(any(PumpDevice.class));
    }

    @Test
    void assignToPump_shouldAssignAndSave_whenAllConditionsMet() {
        // given
        Pump pump = mock(Pump.class);
        when(pump.getId()).thenReturn(pumpId);

        PumpDevice device = mock(PumpDevice.class);
        when(device.getPump()).thenReturn(null);
        when(device.getId()).thenReturn(deviceUuid);
        when(device.getDeviceId()).thenReturn("DEVICE-001");

        when(pumpRepository.findById(pumpId)).thenReturn(Optional.of(pump));
        when(pumpDeviceRepository.findByDeviceId("DEVICE-001")).thenReturn(Optional.of(device));
        when(pumpDeviceRepository.existsByPump_Id(pumpId)).thenReturn(false);
        when(pumpDeviceRepository.save(device)).thenReturn(device);

        // when
        PumpDeviceResponse result = pumpDeviceService.assignToPump(pumpId, "DEVICE-001");

        // then
        verify(device).setPump(pump);
        verify(device).setStatus(DeviceStatusType.ASSIGNED);
        verify(pumpDeviceRepository).save(device);
        assertThat(result).isNotNull();
        assertThat(result.pumpId()).isEqualTo(pumpId);
        assertThat(result.deviceId()).isEqualTo("DEVICE-001");
    }

    @Test
    void assignToPump_shouldTrimDeviceId_whenLookingUp() {
        // given
        Pump pump = mock(Pump.class);
        when(pumpRepository.findById(pumpId)).thenReturn(Optional.of(pump));
        when(pumpDeviceRepository.findByDeviceId("DEVICE-001")).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> pumpDeviceService.assignToPump(pumpId, "  DEVICE-001  "))
                .isInstanceOf(EntityNotFoundException.class);

        verify(pumpDeviceRepository).findByDeviceId("DEVICE-001");
    }


    @Test
    void unassignFromPump_shouldThrow_whenNoDeviceAssignedToPump() {
        // given
        when(pumpDeviceRepository.findByPump_Id(pumpId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> pumpDeviceService.unassignFromPump(pumpId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("No device assigned to pump: " + pumpId);

        verify(pumpDeviceRepository, never()).save(any(PumpDevice.class));
    }

    @Test
    void unassignFromPump_shouldClearPumpAndSetUnassigned_whenDeviceExists() {
        // given
        PumpDevice device = mock(PumpDevice.class);
        when(pumpDeviceRepository.findByPump_Id(pumpId)).thenReturn(Optional.of(device));

        // when
        pumpDeviceService.unassignFromPump(pumpId);

        // then
        verify(device).setPump(null);
        verify(device).setStatus(DeviceStatusType.UNASSIGNED);
        verify(pumpDeviceRepository).save(device);
    }
}