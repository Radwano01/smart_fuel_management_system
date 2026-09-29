package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.config.StripeConfigService;
import com.example.smart_fuel_management_system.dto.StripeConfigResponse;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import com.example.smart_fuel_management_system.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StripeConfigController.class)
@AutoConfigureMockMvc(addFilters = false)
class StripeConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StripeConfigService stripeConfigService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    @Test
    void getConfig_shouldReturnOk() throws Exception {
        // given
        StripeConfigResponse response = new StripeConfigResponse(
                "pk_test_123"
        );

        when(stripeConfigService.getConfig())
                .thenReturn(response);

        // when & then
        mockMvc.perform(
                        get("/api/v1/payments/config")
                )
                .andExpect(status().isOk());

        verify(stripeConfigService).getConfig();
    }
}
