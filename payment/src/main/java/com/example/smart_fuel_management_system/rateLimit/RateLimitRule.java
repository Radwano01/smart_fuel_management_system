package com.example.smart_fuel_management_system.rateLimit;

import java.time.Duration;

public enum RateLimitRule {

    // PAYMENTS
    CREATE_PAYMENT(10, Duration.ofMinutes(1)),
    GET_PAYMENT(10, Duration.ofMinutes(1)),
    PAYMENT_SUCCESS_WEBHOOK(100, Duration.ofMinutes(1)),
    PAYMENT_FAILED_WEBHOOK(100, Duration.ofMinutes(1));

    public final int capacity;
    public final Duration duration;

    RateLimitRule(int capacity, Duration duration) {
        this.capacity = capacity;
        this.duration = duration;
    }

    public static RateLimitRule fromPath(String path, String method) {

        // PAYMENTS
        if (path.equals("/api/v1/payments")
                && method.equals("POST"))
            return CREATE_PAYMENT;

        if (path.equals("/api/v1/payments")
                && method.equals("GET"))
            return GET_PAYMENT;

        if (path.equals("/api/v1/payments/webhook/success")
                && method.equals("POST"))
            return PAYMENT_SUCCESS_WEBHOOK;

        if (path.equals("/api/v1/payments/webhook/failed")
                && method.equals("POST"))
            return PAYMENT_FAILED_WEBHOOK;

        return null;
    }
}