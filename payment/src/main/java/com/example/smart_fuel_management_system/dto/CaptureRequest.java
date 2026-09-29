package com.example.smart_fuel_management_system.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CaptureRequest(

        @NotNull
        String paymentIntentId,

        @NotNull
        @Positive
        BigDecimal amount,

        @NotNull
        @Positive
        BigDecimal liters,

        @NotNull
        @Positive
        BigDecimal pricePerLiter) {
}
