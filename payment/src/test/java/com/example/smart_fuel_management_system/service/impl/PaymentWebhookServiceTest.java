package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.enums.PaymentStatusType;
import com.example.smart_fuel_management_system.service.PaymentService;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookServiceTest {

    @Mock
    private PaymentService paymentService;

    private PaymentWebhookService paymentWebhookService;

    @BeforeEach
    void setUp() {
        paymentWebhookService = new PaymentWebhookService(paymentService);

        ReflectionTestUtils.setField(
                paymentWebhookService,
                "endpointSecret",
                "whsec_test"
        );
    }

    @Test
    void handle_shouldProcessSucceededPayment() {
        // given
        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setId("pi_test_123");

        Event event = mock(Event.class);
        when(event.getType()).thenReturn("payment_intent.succeeded");

        EventDataObjectDeserializer deserializer =
                mock(EventDataObjectDeserializer.class);

        when(event.getDataObjectDeserializer())
                .thenReturn(deserializer);

        when(deserializer.getObject())
                .thenReturn(java.util.Optional.of(paymentIntent));

        try (MockedStatic<Webhook> webhookMock =
                     mockStatic(Webhook.class)) {

            webhookMock.when(() ->
                    Webhook.constructEvent(
                            "payload",
                            "signature",
                            "whsec_test"
                    )
            ).thenReturn(event);

            // when
            paymentWebhookService.handle("payload", "signature");

            // then
            verify(paymentService).handlePaymentEvent(
                    "pi_test_123",
                    PaymentStatusType.SUCCESS
            );
        }
    }

    @Test
    void handle_shouldProcessFailedPayment() {
        // given
        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setId("pi_test_123");

        Event event = mock(Event.class);
        when(event.getType()).thenReturn("payment_intent.payment_failed");

        EventDataObjectDeserializer deserializer =
                mock(EventDataObjectDeserializer.class);

        when(event.getDataObjectDeserializer())
                .thenReturn(deserializer);

        when(deserializer.getObject())
                .thenReturn(java.util.Optional.of(paymentIntent));

        try (MockedStatic<Webhook> webhookMock =
                     mockStatic(Webhook.class)) {

            webhookMock.when(() ->
                    Webhook.constructEvent(
                            "payload",
                            "signature",
                            "whsec_test"
                    )
            ).thenReturn(event);

            // when
            paymentWebhookService.handle("payload", "signature");

            // then
            verify(paymentService).handlePaymentEvent(
                    "pi_test_123",
                    PaymentStatusType.FAILED
            );
        }
    }

    @Test
    void handle_shouldIgnoreIrrelevantEvent() {
        // given
        Event event = mock(Event.class);
        when(event.getType()).thenReturn("charge.succeeded");

        try (MockedStatic<Webhook> webhookMock =
                     mockStatic(Webhook.class)) {

            webhookMock.when(() ->
                    Webhook.constructEvent(
                            "payload",
                            "signature",
                            "whsec_test"
                    )
            ).thenReturn(event);

            // when
            paymentWebhookService.handle("payload", "signature");

            // then
            verifyNoInteractions(paymentService);
        }
    }

    @Test
    void handle_shouldThrowException_whenWebhookConstructionFails() {
        // given
        try (MockedStatic<Webhook> webhookMock =
                     mockStatic(Webhook.class)) {

            webhookMock.when(() ->
                    Webhook.constructEvent(
                            "payload",
                            "signature",
                            "whsec_test"
                    )
            ).thenThrow(new RuntimeException("Invalid signature"));

            // when & then
            org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                            paymentWebhookService.handle("payload", "signature")
                    )
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Stripe webhook processing failed")
                    .hasCauseInstanceOf(RuntimeException.class);

            verifyNoInteractions(paymentService);
        }
    }
}