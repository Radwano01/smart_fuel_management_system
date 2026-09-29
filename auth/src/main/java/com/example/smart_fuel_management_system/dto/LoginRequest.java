package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.LoginMethodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull
        LoginMethodType type,

        @NotBlank
        String identifier,

        @NotBlank
        String password
) {}