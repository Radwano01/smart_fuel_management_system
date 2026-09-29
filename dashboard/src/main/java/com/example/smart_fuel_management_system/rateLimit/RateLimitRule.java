package com.example.smart_fuel_management_system.rateLimit;

import java.time.Duration;

public enum RateLimitRule {

    REGISTER(3, Duration.ofMinutes(10)),
    LOGIN(5, Duration.ofMinutes(1)),
    VERIFY_OTP(5, Duration.ofMinutes(5)),
    RESEND_OTP(3, Duration.ofMinutes(1)),
    FORGOT_PASSWORD(3, Duration.ofMinutes(10)),
    RESET_PASSWORD(5, Duration.ofMinutes(10)),
    CHANGE_PASSWORD(5, Duration.ofMinutes(10)),
    VALIDATE_TOKEN(20, Duration.ofMinutes(1));

    public final int capacity;
    public final Duration duration;

    RateLimitRule(int capacity, Duration duration) {
        this.capacity = capacity;
        this.duration = duration;
    }

    public static RateLimitRule fromPath(String path) {

        if (path.startsWith("/api/v1/auth/register")) {
            return REGISTER;
        }

        if (path.startsWith("/api/v1/auth/login")) {
            return LOGIN;
        }

        if (path.startsWith("/api/v1/auth/verify-otp")) {
            return VERIFY_OTP;
        }

        if (path.startsWith("/api/v1/auth/resend")) {
            return RESEND_OTP;
        }

        if (path.startsWith("/api/v1/auth/forgot-password")) {
            return FORGOT_PASSWORD;
        }

        if (path.startsWith("/api/v1/auth/reset-password")) {
            return RESET_PASSWORD;
        }

        if (path.startsWith("/api/v1/auth/change-password")) {
            return CHANGE_PASSWORD;
        }

        if (path.startsWith("/api/v1/auth/validate")) {
            return VALIDATE_TOKEN;
        }

        return null;
    }
}