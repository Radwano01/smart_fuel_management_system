package com.example.smart_fuel_management_system.rateLimit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisRateLimiter {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisRateLimiter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String key, long limit, long windowSeconds) {

        String redisKey = "rate:" + key;

        Long current = redisTemplate.opsForValue().increment(redisKey);

        if(current == null){
            return false;
        }

        if (current == 1) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
        }

        return current <= limit;
    }
}