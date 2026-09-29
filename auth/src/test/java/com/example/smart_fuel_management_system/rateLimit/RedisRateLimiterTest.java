package com.example.smart_fuel_management_system.rateLimit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void isAllowed_shouldReturnTrue_whenRequestIsWithinLimit() {
        // given
        String key = "login:127.0.0.1";
        long limit = 5L;
        long windowSeconds = 60L;

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment("rate:" + key))
                .thenReturn(3L);

        // when
        boolean result =
                rateLimiter.isAllowed(
                        key,
                        limit,
                        windowSeconds
                );

        // then
        assertThat(result).isTrue();
        verify(valueOperations)
                .increment("rate:" + key);
    }

    @Test
    void isAllowed_shouldReturnFalse_whenRequestExceedsLimit() {
        // given
        String key = "login:127.0.0.1";
        long limit = 5L;
        long windowSeconds = 60L;

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment("rate:" + key))
                .thenReturn(6L);

        // when
        boolean result =
                rateLimiter.isAllowed(
                        key,
                        limit,
                        windowSeconds
                );

        // then
        assertThat(result).isFalse();
    }

    @Test
    void isAllowed_shouldSetExpiration_whenFirstRequestInWindow() {
        // given
        String key = "login:127.0.0.1";
        long limit = 5L;
        long windowSeconds = 60L;

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment("rate:" + key))
                .thenReturn(1L);

        // when
        boolean result =
                rateLimiter.isAllowed(
                        key,
                        limit,
                        windowSeconds
                );

        // then
        assertThat(result).isTrue();

        verify(redisTemplate)
                .expire(
                        "rate:" + key,
                        Duration.ofSeconds(windowSeconds)
                );
    }

    @Test
    void isAllowed_shouldNotSetExpiration_whenRequestIsNotFirst() {
        // given
        String key = "login:127.0.0.1";
        long limit = 5L;
        long windowSeconds = 60L;

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment("rate:" + key))
                .thenReturn(2L);

        // when
        boolean result =
                rateLimiter.isAllowed(
                        key,
                        limit,
                        windowSeconds
                );

        // then
        assertThat(result).isTrue();

        verify(redisTemplate, org.mockito.Mockito.never())
                .expire(
                        "rate:" + key,
                        Duration.ofSeconds(windowSeconds)
                );
    }
}