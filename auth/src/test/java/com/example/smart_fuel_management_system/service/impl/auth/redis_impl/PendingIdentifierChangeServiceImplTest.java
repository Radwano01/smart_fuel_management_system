package com.example.smart_fuel_management_system.service.impl.auth.redis_impl;

import com.example.smart_fuel_management_system.entity.PendingIdentifierChange;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingIdentifierChangeServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private PendingIdentifierChangeServiceImpl service;

    @Test
    void save_shouldStoreChangeByIdAndAuthId() {

        // given
        PendingIdentifierChange change =
                mock(PendingIdentifierChange.class);

        UUID changeId = UUID.randomUUID();
        UUID authId = UUID.randomUUID();

        when(change.getId()).thenReturn(changeId);
        when(change.getAuthId()).thenReturn(authId);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        service.save(change);

        // then
        verify(valueOperations)
                .set(
                        "pending_identifier_change:id:" + changeId,
                        change
                );

        verify(valueOperations)
                .set(
                        "pending_identifier_change:auth:" + authId,
                        changeId.toString()
                );
    }

    @Test
    void findById_shouldReturnChange_whenChangeExists() {

        // given
        UUID changeId = UUID.randomUUID();
        PendingIdentifierChange change =
                mock(PendingIdentifierChange.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(
                "pending_identifier_change:id:" + changeId
        )).thenReturn(change);

        // when
        PendingIdentifierChange result =
                service.findById(changeId);

        // then
        assertThat(result).isSameAs(change);

        verify(valueOperations)
                .get(
                        "pending_identifier_change:id:" + changeId
                );
    }

    @Test
    void findById_shouldThrowException_whenChangeDoesNotExist() {

        // given
        UUID changeId = UUID.randomUUID();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(
                "pending_identifier_change:id:" + changeId
        )).thenReturn(null);

        // when & then
        assertThrows(
                EntityNotFoundException.class,
                () -> service.findById(changeId)
        );
    }

    @Test
    void existsByAuthId_shouldReturnTrue_whenChangeExists() {

        // given
        UUID authId = UUID.randomUUID();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(
                "pending_identifier_change:auth:" + authId
        )).thenReturn(UUID.randomUUID().toString());

        // when
        boolean result =
                service.existsByAuthId(authId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void existsByAuthId_shouldReturnFalse_whenChangeDoesNotExist() {

        // given
        UUID authId = UUID.randomUUID();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(
                "pending_identifier_change:auth:" + authId
        )).thenReturn(null);

        // when
        boolean result =
                service.existsByAuthId(authId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void delete_shouldDeleteChangeByIdAndAuthId() {

        // given
        UUID changeId = UUID.randomUUID();
        UUID authId = UUID.randomUUID();

        PendingIdentifierChange change =
                mock(PendingIdentifierChange.class);

        when(change.getAuthId()).thenReturn(authId);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(
                "pending_identifier_change:id:" + changeId
        )).thenReturn(change);

        // when
        service.delete(changeId);

        // then
        verify(redisTemplate)
                .delete(
                        "pending_identifier_change:id:" + changeId
                );

        verify(redisTemplate)
                .delete(
                        "pending_identifier_change:auth:" + authId
                );
    }
}