package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.pump.PumpResponse;
import com.example.smart_fuel_management_system.entity.Pump;
import com.example.smart_fuel_management_system.enums.PumpConnectionStatusType;
import com.example.smart_fuel_management_system.enums.PumpStatusType;
import com.example.smart_fuel_management_system.repository.PumpRepository;
import com.example.smart_fuel_management_system.service.PumpHeartbeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class PumpHeartbeatServiceImpl implements PumpHeartbeatService {

    private static final Duration HEARTBEAT_TTL = Duration.ofSeconds(45);
    private static final String KEY_PREFIX = "pump:heartbeat:";
    private final StringRedisTemplate redisTemplate;
    private final PumpRepository pumpRepository;

    @Override
    public void recordHeartbeat(UUID pumpId, PumpStatusType status) {
        if (pumpId == null || !pumpRepository.existsById(pumpId)) {
            log.warn("Ignoring heartbeat for unknown pump {}", pumpId);
            return;
        }

        String key = key(pumpId);
        
        redisTemplate.opsForHash().put(
                key,
                "lastHeartbeatReceived",
                OffsetDateTime.now(ZoneOffset.UTC).toString()
        );

        redisTemplate.expire(key, HEARTBEAT_TTL);
    }

    @Override
    public PumpResponse enrich(Pump pump) {
        UUID pumpId = pump.getId();
        return new PumpResponse(
                pumpId,
                pump.getPumpNumber(),
                pump.getDevice() != null
                        ? pump.getDevice().getDeviceId()
                        : null,
                pump.getFuelTypes(),
                getStatus(pumpId),
                getConnectionStatus(pumpId),
                getLastHeartbeatReceived(pumpId)
        );
    }

    @Override
    public PumpConnectionStatusType getConnectionStatus(UUID pumpId) {
        return redisTemplate.hasKey(key(pumpId))
                ? PumpConnectionStatusType.ONLINE
                : PumpConnectionStatusType.OFFLINE;
    }

    @Override
    public OffsetDateTime getLastHeartbeatReceived(UUID pumpId) {
        Object value = redisTemplate.opsForHash()
                .get(key(pumpId), "lastHeartbeatReceived");

        return value == null
                ? null
                : OffsetDateTime.parse(value.toString());
    }

    private PumpStatusType getStatus(UUID pumpId) {
        Object status = redisTemplate.opsForHash()
                .get(key(pumpId), "status");

        if (status == null) {
            return null;
        }

        return PumpStatusType.valueOf(status.toString());
    }

    private String key(UUID pumpId) {
        return KEY_PREFIX + pumpId;
    }
}