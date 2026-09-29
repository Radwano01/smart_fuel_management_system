package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.OtpType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VerifyOtpRequest(
        @NotNull
        OtpType type,

        @NotBlank
        String identifier,

        @NotBlank
        String otp

) {}