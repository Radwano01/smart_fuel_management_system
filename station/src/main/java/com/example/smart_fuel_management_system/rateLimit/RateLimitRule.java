package com.example.smart_fuel_management_system.rateLimit;

import org.springframework.http.HttpMethod;

import java.time.Duration;

public enum RateLimitRule {

    CREATE_STATION(5, Duration.ofMinutes(10)),
    CHANGE_STATION_STATUS(10, Duration.ofMinutes(1)),

    CREATE_FUEL_PRICE(20, Duration.ofMinutes(1)),
    UPDATE_FUEL_PRICE(20, Duration.ofMinutes(1));

    public final int capacity;
    public final Duration duration;

    RateLimitRule(int capacity, Duration duration) {
        this.capacity = capacity;
        this.duration = duration;
    }

    public static RateLimitRule fromPath(String path, String method) {

        if (path.matches("/api/v1/admin/stations")
                && method.equals("POST")) {
            return CREATE_STATION;
        }

        if (path.matches("/api/v1/admin/stations/[^/]+/status")
                && method.equals("PATCH")) {
            return CHANGE_STATION_STATUS;
        }

        if (path.matches("/api/v1/admin/stations/[^/]+/fuel-prices/[^/]+")
                && method.equals("POST")) {
            return CREATE_FUEL_PRICE;
        }

        if (path.matches("/api/v1/admin/stations/[^/]+/fuel-prices/[^/]+")
                && method.equals("PATCH")) {
            return UPDATE_FUEL_PRICE;
        }

        return null;
    }
}

