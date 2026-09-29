package com.example.smart_fuel_management_system.rateLimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisRateLimiterTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void isAllowed_shouldReturnTrue_whenCounterWithinLimit() {
        // given
        when(valueOperations.increment("rate:user:123")).thenReturn(3L);

        // when
        boolean allowed = rateLimiter.isAllowed("user:123", 5, 60);

        // then
        assertThat(allowed).isTrue();
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    void isAllowed_shouldReturnTrue_whenCounterEqualsLimit() {
        // given
        when(valueOperations.increment("rate:user:123")).thenReturn(5L);

        // when
        boolean allowed = rateLimiter.isAllowed("user:123", 5, 60);

        // then
        assertThat(allowed).isTrue();
    }

    @Test
    void isAllowed_shouldReturnFalse_whenCounterExceedsLimit() {
        // given
        when(valueOperations.increment("rate:user:123")).thenReturn(6L);

        // when
        boolean allowed = rateLimiter.isAllowed("user:123", 5, 60);

        // then
        assertThat(allowed).isFalse();
    }

    @Test
    void isAllowed_shouldReturnFalse_whenIncrementReturnsNull() {
        // given
        when(valueOperations.increment("rate:user:123")).thenReturn(null);

        // when
        boolean allowed = rateLimiter.isAllowed("user:123", 5, 60);

        // then
        assertThat(allowed).isFalse();
    }

    @Test
    void isAllowed_shouldNotSetExpiry_whenIncrementReturnsNull() {
        // given
        when(valueOperations.increment("rate:user:123")).thenReturn(null);

        // when
        rateLimiter.isAllowed("user:123", 5, 60);

        // then
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    void isAllowed_shouldSetExpiry_whenFirstIncrement() {
        // given
        when(valueOperations.increment("rate:user:123")).thenReturn(1L);

        // when
        rateLimiter.isAllowed("user:123", 5, 60);

        // then
        verify(redisTemplate).expire(eq("rate:user:123"), eq(Duration.ofSeconds(60)));
    }

    @Test
    void isAllowed_shouldNotSetExpiry_whenCounterAlreadyExists() {
        // given
        when(valueOperations.increment("rate:user:123")).thenReturn(2L);

        // when
        rateLimiter.isAllowed("user:123", 5, 60);

        // then
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    void isAllowed_shouldPrefixKeyWithRate() {
        // given
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // when
        rateLimiter.isAllowed("ip:1.2.3.4", 10, 30);

        // then
        verify(valueOperations).increment("rate:ip:1.2.3.4");
    }
}