package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.enums.PaymentStatusType;
import com.example.smart_fuel_management_system.service.PaymentService;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentWebhookService {

    private final PaymentService paymentService;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    public void handle(String payload, String signature) {

        try {
            Event event = Webhook.constructEvent(payload, signature, endpointSecret);

            switch (event.getType()) {

                case "payment_intent.succeeded" -> {
                    PaymentIntent intent = extractPaymentIntent(event);
                    paymentService.handlePaymentEvent(
                            intent.getId(),
                            PaymentStatusType.SUCCESS
                    );
                }

                case "payment_intent.payment_failed" -> {
                    PaymentIntent intent = extractPaymentIntent(event);
                    paymentService.handlePaymentEvent(
                            intent.getId(),
                            PaymentStatusType.FAILED
                    );
                }

                default -> {
                    // ignore irrelevant events
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Stripe webhook processing failed", e);
        }
    }

    private PaymentIntent extractPaymentIntent(Event event) {
        return (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow();
    }
}