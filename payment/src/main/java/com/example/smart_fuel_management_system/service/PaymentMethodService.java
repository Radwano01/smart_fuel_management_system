package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.payment_method.PaymentMethodDetailsResponse;
import com.example.smart_fuel_management_system.dto.payment_method.PaymentMethodRequest;
import com.stripe.exception.StripeException;

import java.util.List;
import java.util.UUID;

public interface PaymentMethodService {

    void addPaymentMethod(PaymentMethodRequest request, UUID userId) throws StripeException;

    List<PaymentMethodDetailsResponse> getPaymentMethods(UUID userId);

    void setDefault(UUID paymentMethodId, UUID userId);

    void delete(UUID paymentMethodId, UUID userId) throws StripeException;
}
