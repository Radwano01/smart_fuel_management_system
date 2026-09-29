package com.example.smart_fuel_management_system.dto;

public record NotificationEvent(String destination,
                           String fullName,
                           String otp,
                           String token) {
}
