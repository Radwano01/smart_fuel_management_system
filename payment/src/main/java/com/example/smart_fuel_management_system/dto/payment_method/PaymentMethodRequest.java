package com.example.smart_fuel_management_system.dto.payment_method;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PaymentMethodRequest(
        @NotBlank(message = "Payment method ID is required")
        @Pattern(
                regexp = "^pm_[A-Za-z0-9_]+$",
                message = "Invalid Stripe PaymentMethod ID"
        )
        String paymentMethodId
) {
}
