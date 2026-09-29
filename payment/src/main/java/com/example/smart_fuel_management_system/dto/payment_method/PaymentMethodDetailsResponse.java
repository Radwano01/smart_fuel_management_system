package com.example.smart_fuel_management_system.dto.payment_method;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentMethodDetailsResponse(UUID id,
                                           String brand,
                                           String last4,
                                           Integer expMonth,
                                           Integer expYear,
                                           boolean isDefault,
                                           boolean expired,
                                           LocalDateTime createdAt,
                                           LocalDateTime updatedAt) {
}
