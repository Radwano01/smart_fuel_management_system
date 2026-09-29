package com.example.smart_fuel_management_system.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordTokenRequest(

        @NotBlank
        @Email
        String email
) {}