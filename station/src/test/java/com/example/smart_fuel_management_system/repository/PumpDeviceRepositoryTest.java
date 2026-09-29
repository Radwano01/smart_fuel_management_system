package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.entity.Pump;
import com.example.smart_fuel_management_system.entity.PumpDevice;
import com.example.smart_fuel_management_system.entity.Station;
import com.example.smart_fuel_management_system.enums.DeviceStatusType;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.enums.StationStatusType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PumpDeviceRepositoryTest {

    @Autowired
    private PumpDeviceRepository pumpDeviceRepository;

    @Autowired
    private PumpRepository pumpRepository;

    @Autowired
    private StationRepository stationRepository;

    private UUID stationId;
    private UUID pumpOneId;
    private UUID pumpTwoId;
    private String deviceOneId;
    private String deviceTwoId;

    @BeforeEach
    void setUp() {
        stationId = persistStation("Station A");

        pumpOneId = persistPump(1, FuelType.GASOLINE);
        pumpTwoId = persistPump(2, FuelType.DIESEL);

        deviceOneId = "DEVICE-001";
        deviceTwoId = "DEVICE-002";

        pumpDeviceRepository.save(buildPumpDevice(deviceOneId, pumpOneId));
        pumpDeviceRepository.save(buildPumpDevice(deviceTwoId, pumpTwoId));
    }

    @Test
    void findByDeviceId_shouldReturnDevice_whenDeviceIdExists() {
        // given
        String searchDeviceId = deviceOneId;

        // when
        Optional<PumpDevice> result =
                pumpDeviceRepository.findByDeviceId(searchDeviceId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getDeviceId()).isEqualTo(searchDeviceId);
    }

    @Test
    void findByDeviceId_shouldReturnEmpty_whenDeviceIdDoesNotExist() {
        // given
        String searchDeviceId = "DEVICE-NONE";

        // when
        Optional<PumpDevice> result =
                pumpDeviceRepository.findByDeviceId(searchDeviceId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByPump_Id_shouldReturnDevice_whenPumpHasDevice() {
        // given
        UUID searchPumpId = pumpOneId;

        // when
        Optional<PumpDevice> result =
                pumpDeviceRepository.findByPump_Id(searchPumpId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getPump().getId()).isEqualTo(searchPumpId);
    }

    @Test
    void findByPump_Id_shouldReturnEmpty_whenPumpHasNoDevice() {
        // given
        UUID searchPumpId = UUID.randomUUID();

        // when
        Optional<PumpDevice> result =
                pumpDeviceRepository.findByPump_Id(searchPumpId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void existsByPump_Id_shouldReturnTrue_whenPumpHasDevice() {
        // given
        UUID searchPumpId = pumpOneId;

        // when
        boolean exists = pumpDeviceRepository.existsByPump_Id(searchPumpId);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByPump_Id_shouldReturnFalse_whenPumpHasNoDevice() {
        // given
        UUID searchPumpId = UUID.randomUUID();

        // when
        boolean exists = pumpDeviceRepository.existsByPump_Id(searchPumpId);

        // then
        assertThat(exists).isFalse();
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

    private UUID persistPump(int pumpNumber, FuelType fuelType) {
        Station stationRef = stationRepository.getReferenceById(stationId);

        Pump pump = Pump.builder()
                .id(UUID.randomUUID())
                .pumpNumber(pumpNumber)
                .fuelTypes(Collections.singleton(fuelType))
                .station(stationRef)
                .build();
        return pumpRepository.save(pump).getId();
    }

    private PumpDevice buildPumpDevice(String deviceId, UUID pumpId) {
        Pump pumpRef = pumpRepository.getReferenceById(pumpId);

        return PumpDevice.builder()
                .id(UUID.randomUUID())
                .deviceId(deviceId)
                .status(DeviceStatusType.ASSIGNED)
                .pump(pumpRef)
                .build();
    }
}