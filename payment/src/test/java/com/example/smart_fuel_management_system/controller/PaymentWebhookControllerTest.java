package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.service.impl.PaymentWebhookService;
import com.example.smart_fuel_management_system.security.JwtService;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentWebhookController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentWebhookService webhookService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    @Test
    void handle_shouldReturnOk() throws Exception {
        // given
        String payload = """
                {
                    "id": "evt_test_123",
                    "type": "payment_intent.succeeded"
                }
                """;

        String signature = "t=123456,v1=test_signature";

        // when & then
        mockMvc.perform(
                        post("/api/v1/payments/webhook")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Stripe-Signature", signature)
                                .content(payload)
                )
                .andExpect(status().isOk());

        verify(webhookService).handle(payload, signature);
    }
}
