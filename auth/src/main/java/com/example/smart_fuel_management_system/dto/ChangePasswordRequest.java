package com.example.smart_fuel_management_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank
        @Size(min = 8, max = 72)
        String currentPassword,
        @NotBlank
        @Size(min = 8, max = 72)
        String newPassword
) {}