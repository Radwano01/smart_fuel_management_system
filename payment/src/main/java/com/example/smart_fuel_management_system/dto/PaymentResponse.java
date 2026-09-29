package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.PaymentStatusType;

public record PaymentResponse(
        String paymentIntentId,
        PaymentStatusType status
) {}