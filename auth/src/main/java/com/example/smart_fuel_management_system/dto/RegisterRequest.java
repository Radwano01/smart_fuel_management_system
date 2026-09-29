package com.example.smart_fuel_management_system.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(

        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        @NotBlank
        @Size(min = 8, max = 72)
        String password,

        @NotBlank
        @Size(min = 2, max = 100)
        String fullName,

        @NotBlank
        @Pattern(
                regexp = "^5\\d{9}$",
                message = "Phone number must be a valid Turkish mobile number"
        )
        String phoneNumber
) {}