package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.CaptureResponse;
import com.example.smart_fuel_management_system.dto.PaymentRequest;
import com.example.smart_fuel_management_system.dto.PaymentResponse;
import com.example.smart_fuel_management_system.enums.PaymentStatusType;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import com.example.smart_fuel_management_system.security.JWTAuthenticationFilter;
import com.example.smart_fuel_management_system.security.JwtService;
import com.example.smart_fuel_management_system.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentInternalController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentInternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    @MockBean
    private PaymentService paymentService;

    @Test
    void preAuth_shouldReturnCreated() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID fuelSessionId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();
        UUID pumpId = UUID.randomUUID();

        String requestBody = """
                {
                    "userId": "%s",
                    "fuelSessionId": "%s",
                    "vehicleId": "%s",
                    "stationId": "%s",
                    "pumpId": "%s",
                    "fuelType": "GASOLINE",
                    "estimatedLiters": 20,
                    "pricePerLiter": 50,
                    "amount": 1000,
                    "currency": "TRY"
                }
                """.formatted(
                userId,
                fuelSessionId,
                vehicleId,
                stationId,
                pumpId
        );

        PaymentResponse response =
                new PaymentResponse(
                        "pi_test_123",
                        PaymentStatusType.SUCCESS
                );

        when(paymentService.preAuth(any(PaymentRequest.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/v1/internal/payments/pre-auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated());

        verify(paymentService).preAuth(any(PaymentRequest.class));
    }

    @Test
    void capture_shouldReturnOk() throws Exception {
        // given
        String paymentIntentId = "pi_test_123";

        String requestBody = """
                {
                    "paymentIntentId": "%s",
                    "amount": 500,
                    "liters": 10,
                    "pricePerLiter": 50
                }
                """.formatted(paymentIntentId);

        CaptureResponse response =
                CaptureResponse.builder()
                        .paymentIntentId(paymentIntentId)
                        .status(PaymentStatusType.PROCESSING)
                        .capturedAmount(BigDecimal.valueOf(500))
                        .build();

        when(paymentService.capturePayment(any()))
                .thenReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/v1/internal/payments/capture")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk());

        verify(paymentService).capturePayment(any());
    }
}