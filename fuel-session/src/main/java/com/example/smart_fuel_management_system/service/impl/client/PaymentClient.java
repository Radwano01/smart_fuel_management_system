package com.example.smart_fuel_management_system.service.impl.client;

import com.example.smart_fuel_management_system.dto.*;
import com.example.smart_fuel_management_system.exception.PaymentServiceUnavailableException;
import com.example.smart_fuel_management_system.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentClient {

    private static final String BASE_URL = "http://PAYMENT/api/v1/internal/payments";
    private final RestTemplate restTemplate;
    private final JwtService jwtService;

    public PaymentResponse preAuth(PaymentRequest request) {

        HttpHeaders headers =
                generateServiceToken("payment.internal.preauth");

        HttpEntity<PaymentRequest> entity =
                new HttpEntity<>(request, headers);

        try {
            ResponseEntity<PaymentResponse> response =
                    restTemplate.exchange(
                            BASE_URL + "/pre-auth",
                            HttpMethod.POST,
                            entity,
                            PaymentResponse.class
                    );

            PaymentResponse payment = response.getBody();

            if (payment == null) {
                throw new PaymentServiceUnavailableException(
                        "Payment service returned an empty pre-auth response"
                );
            }

            return payment;

        } catch (RestClientException e) {
            throw new PaymentServiceUnavailableException(
                    "Payment service is unavailable during pre-authorization",
                    e
            );
        } catch (PaymentServiceUnavailableException e) {
            throw new PaymentServiceUnavailableException("No PAYMENT instance available");
        }
    }

    public CaptureResponse capture(CaptureRequest request) {

        HttpHeaders headers =
                generateServiceToken("payment.internal.capture");

        HttpEntity<CaptureRequest> entity =
                new HttpEntity<>(request, headers);

        try {
            ResponseEntity<CaptureResponse> response =
                    restTemplate.exchange(
                            BASE_URL + "/capture",
                            HttpMethod.POST,
                            entity,
                            CaptureResponse.class
                    );

            CaptureResponse capture = response.getBody();

            if (capture == null) {
                throw new PaymentServiceUnavailableException(
                        "Payment service returned an empty capture response"
                );
            }

            return capture;

        } catch (RestClientException e) {
            throw new PaymentServiceUnavailableException(
                    "Payment service is unavailable during capture",
                    e
            );
        }catch (PaymentServiceUnavailableException e) {
            throw new PaymentServiceUnavailableException("No PAYMENT instance available");
        }
    }

    private HttpHeaders generateServiceToken(String scope){
        String serviceToken = jwtService.generateServiceToken(
                "fuel-session-service",
                "payment-service",
                scope
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceToken);

        return headers;
    }
}