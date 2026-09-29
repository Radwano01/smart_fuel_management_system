package com.example.smart_fuel_management_system.rateLimit;

import java.time.Duration;

public enum RateLimitRule {

    GET_USER(30, Duration.ofMinutes(1)),
    UPDATE_USER(10, Duration.ofMinutes(1)),
    INTERNAL_GET_USER(200, Duration.ofMinutes(1));

    public final int capacity;
    public final Duration duration;

    RateLimitRule(int capacity, Duration duration) {
        this.capacity = capacity;
        this.duration = duration;
    }

    public static RateLimitRule fromPath(String path, String method) {

        if (path.equals("/api/v1/users") && method.equals("GET")) {
            return GET_USER;
        }

        if (path.equals("/api/v1/users") && method.equals("PATCH")) {
            return UPDATE_USER;
        }

        if (path.startsWith("/api/v1/users/internal") && method.equals("GET")) {
            return INTERNAL_GET_USER;
        }
        return null;
    }
}