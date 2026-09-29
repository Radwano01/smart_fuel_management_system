package com.example.smart_fuel_management_system.dto;


public record LoginResponse(
        String token,
        String refreshToken
) {}