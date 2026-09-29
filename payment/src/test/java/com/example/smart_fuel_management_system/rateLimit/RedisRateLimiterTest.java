package com.example.smart_fuel_management_system.rateLimit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
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
    private RedisRateLimiter redisRateLimiter;

    @Test
    void isAllowed_shouldReturnTrue_whenRequestIsWithinLimit() {
        // given
        String key = "user-123";
        long limit = 5;
        long windowSeconds = 60;

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment("rate:" + key))
                .thenReturn(3L);

        // when
        boolean result = redisRateLimiter.isAllowed(
                key,
                limit,
                windowSeconds
        );

        // then
        assertThat(result).isTrue();
    }

    @Test
    void isAllowed_shouldSetExpiration_whenFirstRequest() {
        // given
        String key = "user-123";
        long limit = 5;
        long windowSeconds = 60;

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment("rate:" + key))
                .thenReturn(1L);

        // when
        boolean result = redisRateLimiter.isAllowed(
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
    void isAllowed_shouldReturnFalse_whenLimitIsExceeded() {
        // given
        String key = "user-123";
        long limit = 5;
        long windowSeconds = 60;

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment("rate:" + key))
                .thenReturn(6L);

        // when
        boolean result = redisRateLimiter.isAllowed(
                key,
                limit,
                windowSeconds
        );

        // then
        assertThat(result).isFalse();
    }

    @Test
    void isAllowed_shouldReturnFalse_whenIncrementReturnsNull() {
        // given
        String key = "user-123";
        long limit = 5;
        long windowSeconds = 60;

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment("rate:" + key))
                .thenReturn(null);

        // when
        boolean result = redisRateLimiter.isAllowed(
                key,
                limit,
                windowSeconds
        );

        // then
        assertThat(result).isFalse();
    }
}
