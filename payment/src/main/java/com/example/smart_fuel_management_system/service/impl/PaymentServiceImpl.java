package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.CaptureRequest;
import com.example.smart_fuel_management_system.dto.CaptureResponse;
import com.example.smart_fuel_management_system.dto.PaymentRequest;
import com.example.smart_fuel_management_system.dto.PaymentResponse;
import com.example.smart_fuel_management_system.entity.OutboxEvent;
import com.example.smart_fuel_management_system.entity.Payment;
import com.example.smart_fuel_management_system.enums.PaymentProvider;
import com.example.smart_fuel_management_system.enums.PaymentStatusType;
import com.example.smart_fuel_management_system.repository.PaymentCustomerRepository;
import com.example.smart_fuel_management_system.repository.PaymentOutboxRepository;
import com.example.smart_fuel_management_system.repository.PaymentRepository;
import com.example.smart_fuel_management_system.repository.projection.PreAuthProjection;
import com.example.smart_fuel_management_system.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentOutboxRepository outboxRepository;
    private final PaymentCustomerRepository paymentCustomerRepository;


    // TODO: Resolve the PaymentProvider dynamically and delegate to the appropriate
    //  PaymentGateway implementation (Stripe, PayPal, Adyen, etc.).
    @Override
    public PaymentResponse preAuth(PaymentRequest request) {

        try {
            PreAuthProjection paymentData =
                    paymentCustomerRepository.findPreAuthData(
                            request.userId(),
                            PaymentProvider.STRIPE
                    ).orElseThrow(() ->
                            new EntityNotFoundException("Stripe customer or default payment method not found"));

            Map<String, Object> params = stripeProcess(request.amount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValueExact(), request, paymentData.getCustomerId(), paymentData.getPaymentMethodId());

            PaymentIntent intent = PaymentIntent.create(params);

            savePayment(intent.getId(), paymentData.getPaymentMethodId(), request);

            return new PaymentResponse(
                    intent.getId(),
                    PaymentStatusType.SUCCESS
            );

        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> stripeProcess(
            long amount,
            PaymentRequest request,
            String customerId,
            String paymentMethod
    ) {
        Map<String, Object> params = new HashMap<>();

        params.put("amount", amount);
        params.put("currency", request.currency());
        params.put("customer", customerId);
        params.put("payment_method", paymentMethod);
        params.put("confirm", true);
        params.put("capture_method", "manual");
        params.put("payment_method_types", java.util.List.of("card"));
        params.put("off_session", true);

        return params;
    }

    @Override
    public CaptureResponse capturePayment(CaptureRequest request) {

        try {
            PaymentIntent intent = PaymentIntent.retrieve(request.paymentIntentId());

            long amountToCapture = request.amount()
                    .setScale(2, RoundingMode.HALF_UP)
                    .movePointRight(2)
                    .longValueExact();

            Map<String, Object> params = new HashMap<>();
            params.put("amount_to_capture", amountToCapture);

            PaymentIntent updatedIntent = intent.capture(params);
            BigDecimal capturedAmount = BigDecimal
                    .valueOf(updatedIntent.getAmountReceived())
                    .movePointLeft(2);

            updatePayment(request);

            return CaptureResponse.builder()
                    .paymentIntentId(updatedIntent.getId())
                    .status(PaymentStatusType.PROCESSING)
                    .capturedAmount(capturedAmount)
                    .build();

        } catch (StripeException e) {
            throw new RuntimeException("Payment capture failed", e);
        }
    }

    @Transactional
    @Override
    public void handlePaymentEvent(String paymentIntentId,
                                   PaymentStatusType status) {

        Payment payment = paymentRepository
                .findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));


        if (payment.getStatus() == status) {
            return;
        }

        if (payment.getStatus() != PaymentStatusType.PROCESSING) {
            return;
        }

        payment.setStatus(status);

        OutboxEvent event = OutboxEvent.builder()
                .eventType(getEventType(status))
                .aggregateId(payment.getId())
                .payload(buildPayload(payment))
                .published(false)
                .build();

        outboxRepository.save(event);
    }

    @Transactional
    @Override
    public void reconcile(Payment payment) throws StripeException {

        PaymentIntent intent = PaymentIntent.retrieve(payment.getPaymentIntentId());

        switch (intent.getStatus()) {
            case "succeeded" ->
                    handlePaymentEvent(intent.getId(), PaymentStatusType.SUCCESS);

            case "requires_payment_method",
                 "canceled" ->
                    handlePaymentEvent(intent.getId(), PaymentStatusType.FAILED);

            case "processing",
                 "requires_capture" -> {
                // Still waiting
            }
        }
    }

    private String getEventType(PaymentStatusType status) {
        return switch (status) {
            case SUCCESS -> "PAYMENT_CAPTURED";
            case FAILED -> "PAYMENT_FAILED";
            case PROCESSING -> "PAYMENT_PROCESSING";
        };
    }

    private void savePayment(String paymentIntentId, String paymentMethodId, PaymentRequest request){
        Payment payment = Payment.builder()
                .paymentIntentId(paymentIntentId)
                .paymentMethodId(paymentMethodId)
                .userId(request.userId())
                .fuelSessionId(request.fuelSessionId())
                .vehicleId(request.vehicleId())
                .stationId(request.stationId())
                .pumpId(request.pumpId())
                .fuelType(request.fuelType())
                .estimatedLiters(request.estimatedLiters())
                .pricePerLiter(request.pricePerLiter())
                .amount(request.amount())
                .currency(request.currency())
                .status(PaymentStatusType.PROCESSING)
                .build();

        paymentRepository.save(payment);
    }

    private void updatePayment(CaptureRequest request) {

        Payment payment = paymentRepository
                .findByPaymentIntentId(request.paymentIntentId())
                .orElseThrow();


        payment.setAmount(request.amount());
        payment.setEstimatedLiters(request.liters());
        payment.setPricePerLiter(request.pricePerLiter());

        paymentRepository.save(payment);
    }

    private String buildPayload(Payment payment) {
        return """
    {
        "paymentId": "%s",
        "userId": "%s",
        "fuelSessionId": "%s",
        "vehicleId": "%s",
        "stationId": "%s",
        "pumpId": "%s",
        "fuelType": "%s",
        "liters": %s,
        "pricePerLiter": %s,
        "amount": %s,
        "currency": "%s",
        "status": "%s"
    }
    """.formatted(
                payment.getId(),
                payment.getUserId(),
                payment.getFuelSessionId(),
                payment.getVehicleId(),
                payment.getStationId(),
                payment.getPumpId(),
                payment.getFuelType(),
                payment.getEstimatedLiters(),
                payment.getPricePerLiter(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus()
        );
    }
}