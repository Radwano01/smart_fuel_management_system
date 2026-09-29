package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.controller.fuelPrice.FuelPriceController;
import com.example.smart_fuel_management_system.dto.fuelPrice.FuelPriceResponse;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import com.example.smart_fuel_management_system.security.JwtService;
import com.example.smart_fuel_management_system.service.FuelPriceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FuelPriceController.class)
@AutoConfigureMockMvc(addFilters = false)
class FuelPriceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FuelPriceService fuelPriceService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    @Test
    void getPrice_shouldReturnOk_whenStationIdIsUuid() throws Exception {
        // given
        UUID stationId = UUID.randomUUID();
        List<FuelPriceResponse> body = List.of(mock(FuelPriceResponse.class));
        when(fuelPriceService.getStationPrices(stationId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/stations/{stationId}/fuel-types", stationId));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
        verify(fuelPriceService).getStationPrices(stationId);
    }

    @Test
    void getPrice_shouldReturnOk_whenStationHasNoPrices() throws Exception {
        // given
        UUID stationId = UUID.randomUUID();
        when(fuelPriceService.getStationPrices(stationId)).thenReturn(List.of());

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/stations/{stationId}/fuel-types", stationId));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        verify(fuelPriceService).getStationPrices(stationId);
    }

    @Test
    void getPrice_shouldThrow_whenStationIdIsNotUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                get("/api/v1/stations/{stationId}/fuel-types", malformed)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(fuelPriceService);
    }
}