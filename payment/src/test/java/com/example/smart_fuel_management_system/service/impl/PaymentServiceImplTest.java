package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.CaptureRequest;
import com.example.smart_fuel_management_system.dto.CaptureResponse;
import com.example.smart_fuel_management_system.dto.PaymentRequest;
import com.example.smart_fuel_management_system.dto.PaymentResponse;
import com.example.smart_fuel_management_system.entity.OutboxEvent;
import com.example.smart_fuel_management_system.entity.Payment;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.enums.PaymentProvider;
import com.example.smart_fuel_management_system.enums.PaymentStatusType;
import com.example.smart_fuel_management_system.repository.PaymentCustomerRepository;
import com.example.smart_fuel_management_system.repository.PaymentOutboxRepository;
import com.example.smart_fuel_management_system.repository.PaymentRepository;
import com.example.smart_fuel_management_system.repository.projection.PreAuthProjection;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentOutboxRepository outboxRepository;

    @Mock
    private PaymentCustomerRepository paymentCustomerRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void preAuth_shouldCreatePaymentAndSaveIt() throws StripeException {
        // given
        UUID userId = UUID.randomUUID();
        UUID fuelSessionId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();
        UUID pumpId = UUID.randomUUID();

        PaymentRequest request = mock(PaymentRequest.class);

        when(request.userId()).thenReturn(userId);
        when(request.fuelSessionId()).thenReturn(fuelSessionId);
        when(request.vehicleId()).thenReturn(vehicleId);
        when(request.stationId()).thenReturn(stationId);
        when(request.pumpId()).thenReturn(pumpId);
        when(request.fuelType()).thenReturn(FuelType.GASOLINE);
        when(request.estimatedLiters()).thenReturn(BigDecimal.TEN);
        when(request.pricePerLiter()).thenReturn(BigDecimal.valueOf(50));
        when(request.amount()).thenReturn(BigDecimal.valueOf(500));
        when(request.currency()).thenReturn("try");

        PreAuthProjection paymentData = mock(PreAuthProjection.class);

        when(paymentData.getCustomerId()).thenReturn("cus_test");
        when(paymentData.getPaymentMethodId()).thenReturn("pm_test");

        when(paymentCustomerRepository.findPreAuthData(
                userId,
                PaymentProvider.STRIPE
        )).thenReturn(Optional.of(paymentData));

        PaymentIntent intent = mock(PaymentIntent.class);

        when(intent.getId()).thenReturn("pi_test_123");

        try (MockedStatic<PaymentIntent> paymentIntentMock =
                     mockStatic(PaymentIntent.class)) {

            paymentIntentMock.when(() ->
                    PaymentIntent.create(anyMap())
            ).thenReturn(intent);

            // when
            PaymentResponse response = paymentService.preAuth(request);

            // then
            assertThat(response.paymentIntentId())
                    .isEqualTo("pi_test_123");
            assertThat(response.status())
                    .isEqualTo(PaymentStatusType.SUCCESS);

            ArgumentCaptor<Payment> captor =
                    ArgumentCaptor.forClass(Payment.class);

            verify(paymentRepository).save(captor.capture());

            Payment payment = captor.getValue();

            assertThat(payment.getPaymentIntentId())
                    .isEqualTo("pi_test_123");
            assertThat(payment.getPaymentMethodId())
                    .isEqualTo("pm_test");
            assertThat(payment.getUserId())
                    .isEqualTo(userId);
            assertThat(payment.getFuelSessionId())
                    .isEqualTo(fuelSessionId);
            assertThat(payment.getVehicleId())
                    .isEqualTo(vehicleId);
            assertThat(payment.getStationId())
                    .isEqualTo(stationId);
            assertThat(payment.getPumpId())
                    .isEqualTo(pumpId);
            assertThat(payment.getFuelType())
                    .isEqualTo(FuelType.GASOLINE);
            assertThat(payment.getAmount())
                    .isEqualByComparingTo("500");
            assertThat(payment.getCurrency())
                    .isEqualTo("try");
            assertThat(payment.getStatus())
                    .isEqualTo(PaymentStatusType.PROCESSING);
        }
    }

    @Test
    void preAuth_shouldThrowException_whenCustomerOrPaymentMethodDoesNotExist() {
        // given
        UUID userId = UUID.randomUUID();
        PaymentRequest request = mock(PaymentRequest.class);

        when(request.userId()).thenReturn(userId);

        when(paymentCustomerRepository.findPreAuthData(
                userId,
                PaymentProvider.STRIPE
        )).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                paymentService.preAuth(request)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Stripe customer or default payment method not found");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void capturePayment_shouldCapturePaymentAndUpdatePayment() throws StripeException {
        // given
        UUID userId = UUID.randomUUID();

        CaptureRequest request = mock(CaptureRequest.class);

        when(request.paymentIntentId()).thenReturn("pi_test_123");
        when(request.amount()).thenReturn(BigDecimal.valueOf(123.456));
        when(request.liters()).thenReturn(BigDecimal.valueOf(10.5));
        when(request.pricePerLiter()).thenReturn(BigDecimal.valueOf(11.76));

        Payment payment = new Payment();
        payment.setPaymentIntentId("pi_test_123");

        when(paymentRepository.findByPaymentIntentId("pi_test_123"))
                .thenReturn(Optional.of(payment));

        PaymentIntent intent = mock(PaymentIntent.class);
        PaymentIntent updatedIntent = mock(PaymentIntent.class);

        when(intent.capture(anyMap())).thenReturn(updatedIntent);
        when(updatedIntent.getId()).thenReturn("pi_test_123");
        when(updatedIntent.getAmountReceived()).thenReturn(12346L);

        try (MockedStatic<PaymentIntent> paymentIntentMock =
                     mockStatic(PaymentIntent.class)) {

            paymentIntentMock.when(() ->
                    PaymentIntent.retrieve("pi_test_123")
            ).thenReturn(intent);

            // when
            CaptureResponse response =
                    paymentService.capturePayment(request);

            // then
            assertThat(response.paymentIntentId())
                    .isEqualTo("pi_test_123");
            assertThat(response.status())
                    .isEqualTo(PaymentStatusType.PROCESSING);
            assertThat(response.capturedAmount())
                    .isEqualByComparingTo("123.46");

            assertThat(payment.getAmount())
                    .isEqualByComparingTo("123.456");
            assertThat(payment.getEstimatedLiters())
                    .isEqualByComparingTo("10.5");
            assertThat(payment.getPricePerLiter())
                    .isEqualByComparingTo("11.76");

            verify(paymentRepository).save(payment);
        }
    }

    @Test
    void handlePaymentEvent_shouldUpdatePaymentAndCreateOutboxEvent_whenPaymentIsProcessing() {
        // given
        UUID paymentId = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setPaymentIntentId("pi_test_123");
        payment.setStatus(PaymentStatusType.PROCESSING);
        payment.setUserId(UUID.randomUUID());
        payment.setFuelSessionId(UUID.randomUUID());
        payment.setVehicleId(UUID.randomUUID());
        payment.setStationId(UUID.randomUUID());
        payment.setPumpId(UUID.randomUUID());
        payment.setFuelType(FuelType.GASOLINE);
        payment.setEstimatedLiters(BigDecimal.TEN);
        payment.setPricePerLiter(BigDecimal.valueOf(50));
        payment.setAmount(BigDecimal.valueOf(500));
        payment.setCurrency("TRY");

        when(paymentRepository.findByPaymentIntentId("pi_test_123"))
                .thenReturn(Optional.of(payment));

        // when
        paymentService.handlePaymentEvent(
                "pi_test_123",
                PaymentStatusType.SUCCESS
        );

        // then
        assertThat(payment.getStatus())
                .isEqualTo(PaymentStatusType.SUCCESS);

        ArgumentCaptor<OutboxEvent> captor =
                ArgumentCaptor.forClass(OutboxEvent.class);

        verify(outboxRepository).save(captor.capture());

        OutboxEvent event = captor.getValue();

        assertThat(event.getEventType())
                .isEqualTo("PAYMENT_CAPTURED");
        assertThat(event.getAggregateId())
                .isEqualTo(paymentId);
        assertThat(event.isPublished())
                .isFalse();
        assertThat(event.getPayload())
                .contains("\"paymentId\": \"" + paymentId + "\"")
                .contains("\"status\": \"SUCCESS\"");
    }

    @Test
    void handlePaymentEvent_shouldDoNothing_whenPaymentAlreadyHasSameStatus() {
        // given
        Payment payment = new Payment();
        payment.setStatus(PaymentStatusType.SUCCESS);

        when(paymentRepository.findByPaymentIntentId("pi_test_123"))
                .thenReturn(Optional.of(payment));

        // when
        paymentService.handlePaymentEvent(
                "pi_test_123",
                PaymentStatusType.SUCCESS
        );

        // then
        verify(outboxRepository, never()).save(any());
    }

    @Test
    void handlePaymentEvent_shouldDoNothing_whenPaymentIsNotProcessing() {
        // given
        Payment payment = new Payment();
        payment.setStatus(PaymentStatusType.SUCCESS);

        when(paymentRepository.findByPaymentIntentId("pi_test_123"))
                .thenReturn(Optional.of(payment));

        // when
        paymentService.handlePaymentEvent(
                "pi_test_123",
                PaymentStatusType.FAILED
        );

        // then
        assertThat(payment.getStatus())
                .isEqualTo(PaymentStatusType.SUCCESS);

        verify(outboxRepository, never()).save(any());
    }

    @Test
    void reconcile_shouldHandleSucceededPayment() throws StripeException {
        // given
        Payment payment = new Payment();
        payment.setPaymentIntentId("pi_test_123");
        payment.setStatus(PaymentStatusType.PROCESSING);

        PaymentIntent intent = mock(PaymentIntent.class);

        when(intent.getId()).thenReturn("pi_test_123");
        when(intent.getStatus()).thenReturn("succeeded");

        when(paymentRepository.findByPaymentIntentId("pi_test_123"))
                .thenReturn(Optional.of(payment));

        try (MockedStatic<PaymentIntent> paymentIntentMock =
                     mockStatic(PaymentIntent.class)) {

            paymentIntentMock.when(() ->
                    PaymentIntent.retrieve("pi_test_123")
            ).thenReturn(intent);

            // when
            paymentService.reconcile(payment);

            // then
            assertThat(payment.getStatus())
                    .isEqualTo(PaymentStatusType.SUCCESS);

            verify(outboxRepository).save(any(OutboxEvent.class));
        }
    }
}