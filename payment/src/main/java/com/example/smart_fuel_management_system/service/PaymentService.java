package com.example.smart_fuel_management_system.service;


import com.example.smart_fuel_management_system.dto.CaptureRequest;
import com.example.smart_fuel_management_system.dto.CaptureResponse;
import com.example.smart_fuel_management_system.dto.PaymentRequest;
import com.example.smart_fuel_management_system.dto.PaymentResponse;
import com.example.smart_fuel_management_system.entity.Payment;
import com.example.smart_fuel_management_system.enums.PaymentStatusType;
import com.stripe.exception.StripeException;
import jakarta.transaction.Transactional;

public interface PaymentService {
    PaymentResponse preAuth(PaymentRequest request);
    CaptureResponse capturePayment(CaptureRequest request);
    void handlePaymentEvent(String paymentIntentId,
                            PaymentStatusType status);
    void reconcile(Payment payment) throws StripeException;
}