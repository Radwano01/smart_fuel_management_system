package com.example.smart_fuel_management_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RegisterStationRequest(
        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        @NotBlank
        @Size(min = 2, max = 100)
        String fullName,

        @NotBlank
        @Pattern(
                regexp = "^5\\d{9}$",
                message = "Phone number must be a valid Turkish mobile number (e.g. 5551234567)"
        )
        String phoneNumber,

        @NotBlank
        @Size(min = 8, max = 72)
        String password
) {
}
