package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.PaymentStatusType;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(
        String paymentIntentId,
        PaymentStatusType status
) {}