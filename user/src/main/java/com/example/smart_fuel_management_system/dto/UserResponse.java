package com.example.smart_fuel_management_system.dto;

import java.util.UUID;

public record UserResponse(UUID id,
                           String fullName) {}
