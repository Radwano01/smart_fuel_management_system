package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.PaymentStatusType;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CaptureResponse(
        String paymentIntentId,
        PaymentStatusType status,
        BigDecimal capturedAmount
) {}