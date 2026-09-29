package com.example.smart_fuel_management_system.service.impl.client;

import com.example.smart_fuel_management_system.dto.CaptureRequest;
import com.example.smart_fuel_management_system.dto.CaptureResponse;
import com.example.smart_fuel_management_system.dto.PaymentRequest;
import com.example.smart_fuel_management_system.dto.PaymentResponse;
import com.example.smart_fuel_management_system.exception.PaymentServiceUnavailableException;
import com.example.smart_fuel_management_system.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JwtService jwtService;

    @Mock
    private ResponseEntity<PaymentResponse> paymentResponse;

    @Mock
    private ResponseEntity<CaptureResponse> captureResponse;

    @InjectMocks
    private PaymentClient paymentClient;

    @Test
    void preAuth_shouldReturnPayment_whenPaymentServiceSucceeds() {
        // given
        PaymentRequest request =
                org.mockito.Mockito.mock(PaymentRequest.class);

        PaymentResponse payment =
                org.mockito.Mockito.mock(PaymentResponse.class);

        when(jwtService.generateServiceToken(
                "fuel-session-service",
                "payment-service",
                "payment.internal.preauth"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                eq("http://PAYMENT/api/v1/internal/payments/pre-auth"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(PaymentResponse.class)
        )).thenReturn(paymentResponse);

        when(paymentResponse.getBody())
                .thenReturn(payment);

        // when
        PaymentResponse result =
                paymentClient.preAuth(request);

        // then
        assertThat(result)
                .isSameAs(payment);
    }

    @Test
    void preAuth_shouldThrowPaymentServiceUnavailableException_whenResponseBodyIsNull() {
        // given
        PaymentRequest request =
                org.mockito.Mockito.mock(PaymentRequest.class);

        when(jwtService.generateServiceToken(
                "fuel-session-service",
                "payment-service",
                "payment.internal.preauth"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                eq("http://PAYMENT/api/v1/internal/payments/pre-auth"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(PaymentResponse.class)
        )).thenReturn(paymentResponse);

        when(paymentResponse.getBody())
                .thenReturn(null);

        // when & then
        assertThatThrownBy(() ->
                paymentClient.preAuth(request)
        ).isInstanceOf(PaymentServiceUnavailableException.class)
                .hasMessage("No PAYMENT instance available");
    }

    @Test
    void preAuth_shouldThrowPaymentServiceUnavailableException_whenRestClientThrowsException() {
        // given
        PaymentRequest request =
                org.mockito.Mockito.mock(PaymentRequest.class);

        when(jwtService.generateServiceToken(
                "fuel-session-service",
                "payment-service",
                "payment.internal.preauth"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                eq("http://PAYMENT/api/v1/internal/payments/pre-auth"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(PaymentResponse.class)
        )).thenThrow(
                new RestClientException("Payment service unavailable")
        );

        // when & then
        assertThatThrownBy(() ->
                paymentClient.preAuth(request)
        ).isInstanceOf(PaymentServiceUnavailableException.class)
                .hasMessage(
                        "Payment service is unavailable during pre-authorization"
                );
    }

    @Test
    void capture_shouldReturnCapture_whenPaymentServiceSucceeds() {
        // given
        CaptureRequest request =
                org.mockito.Mockito.mock(CaptureRequest.class);

        CaptureResponse capture =
                org.mockito.Mockito.mock(CaptureResponse.class);

        when(jwtService.generateServiceToken(
                "fuel-session-service",
                "payment-service",
                "payment.internal.capture"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                eq("http://PAYMENT/api/v1/internal/payments/capture"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(CaptureResponse.class)
        )).thenReturn(captureResponse);

        when(captureResponse.getBody())
                .thenReturn(capture);

        // when
        CaptureResponse result =
                paymentClient.capture(request);

        // then
        assertThat(result)
                .isSameAs(capture);
    }

    @Test
    void capture_shouldThrowPaymentServiceUnavailableException_whenResponseBodyIsNull() {
        // given
        CaptureRequest request =
                org.mockito.Mockito.mock(CaptureRequest.class);

        when(jwtService.generateServiceToken(
                "fuel-session-service",
                "payment-service",
                "payment.internal.capture"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                eq("http://PAYMENT/api/v1/internal/payments/capture"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(CaptureResponse.class)
        )).thenReturn(captureResponse);

        when(captureResponse.getBody())
                .thenReturn(null);

        // when & then
        assertThatThrownBy(() ->
                paymentClient.capture(request)
        ).isInstanceOf(PaymentServiceUnavailableException.class)
                .hasMessage("No PAYMENT instance available");
    }

    @Test
    void capture_shouldThrowPaymentServiceUnavailableException_whenRestClientThrowsException() {
        // given
        CaptureRequest request =
                org.mockito.Mockito.mock(CaptureRequest.class);

        when(jwtService.generateServiceToken(
                "fuel-session-service",
                "payment-service",
                "payment.internal.capture"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                eq("http://PAYMENT/api/v1/internal/payments/capture"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(CaptureResponse.class)
        )).thenThrow(
                new RestClientException("Payment service unavailable")
        );

        // when & then
        assertThatThrownBy(() ->
                paymentClient.capture(request)
        ).isInstanceOf(PaymentServiceUnavailableException.class)
                .hasMessage("Payment service is unavailable during capture");
    }
}
