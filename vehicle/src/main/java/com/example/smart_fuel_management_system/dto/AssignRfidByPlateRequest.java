package com.example.smart_fuel_management_system.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignRfidByPlateRequest(
        @NotBlank String rfid
) {
}