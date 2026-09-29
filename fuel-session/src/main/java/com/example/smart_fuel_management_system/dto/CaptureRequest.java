package com.example.smart_fuel_management_system.dto;


import java.math.BigDecimal;

public record CaptureRequest(
        String paymentIntentId,
        BigDecimal amount,
        BigDecimal liters,
        BigDecimal pricePerLiter) {
}
