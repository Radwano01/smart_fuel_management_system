package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.payment_method.PaymentMethodDetailsResponse;
import com.example.smart_fuel_management_system.dto.payment_method.PaymentMethodRequest;
import com.example.smart_fuel_management_system.security.JwtService;
import com.example.smart_fuel_management_system.security.RateLimitingFilter;
import com.example.smart_fuel_management_system.service.PaymentMethodService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RateLimitingFilter rateLimitingFilter;

    @MockBean
    private PaymentMethodService paymentMethodService;

    @Test
    void addPaymentMethod_shouldReturnCreated() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        String requestBody = """
                {
                    "paymentMethodId": "pm_test_123"
                }
                """;

        // when & then
        mockMvc.perform(
                        post("/api/v1/payments")
                                .principal(() -> userId.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated());

        verify(paymentMethodService).addPaymentMethod(
                any(PaymentMethodRequest.class),
                org.mockito.ArgumentMatchers.eq(userId)
        );
    }

    @Test
    void getPaymentMethods_shouldReturnPaymentMethods() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        PaymentMethodDetailsResponse response =
                new PaymentMethodDetailsResponse(
                        UUID.randomUUID(),
                        "visa",
                        "4242",
                        12,
                        2030,
                        true,
                        false,
                        null,
                        null
                );

        when(paymentMethodService.getPaymentMethods(userId))
                .thenReturn(List.of(response));

        // when & then
        mockMvc.perform(
                        get("/api/v1/payments")
                                .principal(() -> userId.toString())
                )
                .andExpect(status().isOk());

        verify(paymentMethodService).getPaymentMethods(userId);
    }

    @Test
    void setDefault_shouldReturnNoContent() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID paymentMethodId = UUID.randomUUID();

        // when & then
        mockMvc.perform(
                        patch("/api/v1/payments/{id}/default", paymentMethodId)
                                .principal(() -> userId.toString())
                )
                .andExpect(status().isNoContent());

        verify(paymentMethodService)
                .setDefault(paymentMethodId, userId);
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID paymentMethodId = UUID.randomUUID();

        // when & then
        mockMvc.perform(
                        delete("/api/v1/payments/{id}", paymentMethodId)
                                .principal(() -> userId.toString())
                )
                .andExpect(status().isNoContent());

        verify(paymentMethodService)
                .delete(paymentMethodId, userId);
    }
}
