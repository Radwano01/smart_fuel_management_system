package com.example.smart_fuel_management_system.service.impl.auth.redis_impl;

import com.example.smart_fuel_management_system.entity.PendingIdentifierChange;
import com.example.smart_fuel_management_system.service.PendingIdentifierChangeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PendingIdentifierChangeServiceImpl
        implements PendingIdentifierChangeService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CHANGE_KEY =
            "pending_identifier_change:id:";

    private static final String AUTH_KEY =
            "pending_identifier_change:auth:";


    @Override
    public void save(PendingIdentifierChange change) {

        String id = change.getId().toString();

        redisTemplate.opsForValue()
                .set(CHANGE_KEY + id, change);

        redisTemplate.opsForValue()
                .set(AUTH_KEY + change.getAuthId(), id);
    }


    @Override
    public PendingIdentifierChange findById(UUID id) {

        PendingIdentifierChange change =
                (PendingIdentifierChange)
                        redisTemplate.opsForValue()
                                .get(CHANGE_KEY + id);

        if (change == null) {
            throw new EntityNotFoundException(
                    "Pending identifier change not found or expired"
            );
        }

        return change;
    }


    @Override
    public boolean existsByAuthId(UUID authId) {

        return redisTemplate.opsForValue()
                .get(AUTH_KEY + authId) != null;
    }


    @Override
    public void delete(UUID id) {

        PendingIdentifierChange change = findById(id);

        redisTemplate.delete(
                CHANGE_KEY + id
        );

        redisTemplate.delete(
                AUTH_KEY + change.getAuthId()
        );
    }
}