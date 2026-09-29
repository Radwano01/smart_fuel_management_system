package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.pump.PumpResponse;
import com.example.smart_fuel_management_system.entity.Pump;
import com.example.smart_fuel_management_system.entity.PumpDevice;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.enums.PumpConnectionStatusType;
import com.example.smart_fuel_management_system.enums.PumpStatusType;
import com.example.smart_fuel_management_system.repository.PumpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PumpHeartbeatServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private PumpRepository pumpRepository;

    @InjectMocks
    private PumpHeartbeatServiceImpl pumpHeartbeatService;

    private UUID pumpId;
    private String key;

    @BeforeEach
    void setUp() {
        pumpId = UUID.randomUUID();
        key = "pump:heartbeat:" + pumpId;

        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    void recordHeartbeat_shouldIgnore_whenPumpIdIsNull() {
        // when
        pumpHeartbeatService.recordHeartbeat(null, PumpStatusType.ONLINE);

        // then
        verifyNoInteractions(redisTemplate, pumpRepository);
    }

    @Test
    void recordHeartbeat_shouldIgnore_whenPumpDoesNotExist() {
        // given
        when(pumpRepository.existsById(pumpId)).thenReturn(false);

        // when
        pumpHeartbeatService.recordHeartbeat(pumpId, PumpStatusType.ONLINE);

        // then
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void recordHeartbeat_shouldStoreUtcTimestampAndSetTtl_whenPumpExists() {
        // given
        when(pumpRepository.existsById(pumpId)).thenReturn(true);

        // when
        pumpHeartbeatService.recordHeartbeat(pumpId, PumpStatusType.ONLINE);

        // then
        ArgumentCaptor<String> timestampCaptor = ArgumentCaptor.forClass(String.class);
        verify(hashOperations).put(
                eq(key),
                eq("lastHeartbeatReceived"),
                timestampCaptor.capture());

        OffsetDateTime parsed = OffsetDateTime.parse(timestampCaptor.getValue());
        assertThat(parsed.getOffset()).isEqualTo(ZoneOffset.UTC);

        verify(redisTemplate).expire(key, Duration.ofSeconds(45));
    }

    @Test
    void recordHeartbeat_shouldNotStoreStatusField_whenStatusProvided() {
        // given
        when(pumpRepository.existsById(pumpId)).thenReturn(true);

        // when
        pumpHeartbeatService.recordHeartbeat(pumpId, PumpStatusType.FUELING);

        // then
        verify(hashOperations, never()).put(anyString(), eq("status"), any());
    }

    @Test
    void getConnectionStatus_shouldReturnOnline_whenKeyExists() {
        // given
        when(redisTemplate.hasKey(key)).thenReturn(true);

        // when
        PumpConnectionStatusType result = pumpHeartbeatService.getConnectionStatus(pumpId);

        // then
        assertThat(result).isEqualTo(PumpConnectionStatusType.ONLINE);
    }

    @Test
    void getConnectionStatus_shouldReturnOffline_whenKeyMissing() {
        // given
        when(redisTemplate.hasKey(key)).thenReturn(false);

        // when
        PumpConnectionStatusType result = pumpHeartbeatService.getConnectionStatus(pumpId);

        // then
        assertThat(result).isEqualTo(PumpConnectionStatusType.OFFLINE);
    }

    @Test
    void getLastHeartbeatReceived_shouldReturnParsedOffsetDateTime_whenValuePresent() {
        // given
        OffsetDateTime expected = OffsetDateTime.now(ZoneOffset.UTC).minusSeconds(10);
        when(hashOperations.get(key, "lastHeartbeatReceived")).thenReturn(expected.toString());

        // when
        OffsetDateTime result = pumpHeartbeatService.getLastHeartbeatReceived(pumpId);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getLastHeartbeatReceived_shouldReturnNull_whenValueMissing() {
        // given
        when(hashOperations.get(key, "lastHeartbeatReceived")).thenReturn(null);

        // when
        OffsetDateTime result = pumpHeartbeatService.getLastHeartbeatReceived(pumpId);

        // then
        assertThat(result).isNull();
    }

    @Test
    void enrich_shouldMapAllFields_whenDataPresent() {
        // given
        Pump pump = mock(Pump.class);
        PumpDevice device = mock(PumpDevice.class);
        OffsetDateTime lastHeartbeat = OffsetDateTime.now(ZoneOffset.UTC).minusSeconds(5);

        when(pump.getId()).thenReturn(pumpId);
        when(pump.getPumpNumber()).thenReturn(7L);
        when(pump.getDevice()).thenReturn(device);
        when(device.getDeviceId()).thenReturn("DEVICE-001");
        when(pump.getFuelTypes()).thenReturn(Set.of(FuelType.GASOLINE, FuelType.DIESEL));

        when(redisTemplate.hasKey(key)).thenReturn(true);
        when(hashOperations.get(key, "status")).thenReturn(PumpStatusType.FUELING.name());
        when(hashOperations.get(key, "lastHeartbeatReceived")).thenReturn(lastHeartbeat.toString());

        // when
        PumpResponse response = pumpHeartbeatService.enrich(pump);

        // then
        assertThat(response.id()).isEqualTo(pumpId);
        assertThat(response.pumpNumber()).isEqualTo(7L);
        assertThat(response.deviceId()).isEqualTo("DEVICE-001");
        assertThat(response.fuelTypes())
                .containsExactlyInAnyOrder(FuelType.GASOLINE, FuelType.DIESEL);
        assertThat(response.status()).isEqualTo(PumpStatusType.FUELING);
        assertThat(response.connectionStatus()).isEqualTo(PumpConnectionStatusType.ONLINE);
        assertThat(response.lastHeartbeatReceived()).isEqualTo(lastHeartbeat);
    }

    @Test
    void enrich_shouldReturnNullOptionalFields_whenDataMissing() {
        // given
        Pump pump = mock(Pump.class);

        when(pump.getId()).thenReturn(pumpId);
        when(pump.getPumpNumber()).thenReturn(3L);
        when(pump.getFuelTypes()).thenReturn(Set.of(FuelType.GASOLINE));

        when(redisTemplate.hasKey(key)).thenReturn(false);
        when(hashOperations.get(key, "status")).thenReturn(null);
        when(hashOperations.get(key, "lastHeartbeatReceived")).thenReturn(null);

        // when
        PumpResponse response = pumpHeartbeatService.enrich(pump);

        // then
        assertThat(response.id()).isEqualTo(pumpId);
        assertThat(response.deviceId()).isNull();
        assertThat(response.status()).isNull();
        assertThat(response.connectionStatus()).isEqualTo(PumpConnectionStatusType.OFFLINE);
        assertThat(response.lastHeartbeatReceived()).isNull();
    }
}