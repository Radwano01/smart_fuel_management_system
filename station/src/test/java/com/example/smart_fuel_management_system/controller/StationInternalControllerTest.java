package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.controller.station.StationInternalController;
import com.example.smart_fuel_management_system.dto.station.StationDashboardSummaryResponse;
import com.example.smart_fuel_management_system.dto.station.StationTransactionResponse;
import com.example.smart_fuel_management_system.dto.station.StationValidationResponse;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import com.example.smart_fuel_management_system.security.JwtService;
import com.example.smart_fuel_management_system.service.FuelPriceService;
import com.example.smart_fuel_management_system.service.StationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StationInternalController.class)
@AutoConfigureMockMvc(addFilters = false)
class StationInternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FuelPriceService fuelPriceService;

    @MockBean
    private StationService stationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    @Test
    void getFuelPrice_shouldReturnOk_whenStationIdAndFuelTypeAreValid() throws Exception {
        // given
        UUID stationId = UUID.randomUUID();
        BigDecimal body = new BigDecimal("50.00");
        when(fuelPriceService.getPrice(stationId, FuelType.GASOLINE)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/stations/{id}/fuel-prices/{fuelType}",
                        stationId, "GASOLINE"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").value(50.00));
        verify(fuelPriceService).getPrice(stationId, FuelType.GASOLINE);
    }

    @Test
    void getFuelPrice_shouldThrow_whenStationIdIsNotUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                get("/api/v1/internal/stations/{id}/fuel-prices/{fuelType}",
                        malformed, "GASOLINE")))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(fuelPriceService, stationService);
    }

    @Test
    void getFuelPrice_shouldReject_whenFuelTypeIsInvalidEnum() throws Exception {
        // given
        UUID stationId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/stations/{id}/fuel-prices/{fuelType}",
                        stationId, "NOT_A_FUEL"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(fuelPriceService, stationService);
    }

    @Test
    void getStationsByIds_shouldReturnOk_whenBodyIsListOfUuids() throws Exception {
        // given
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        List<StationTransactionResponse> body =
                List.of(mock(StationTransactionResponse.class));
        String json = objectMapper.writeValueAsString(ids);
        when(stationService.getStationsByIds(ids)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/internal/stations/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
        verify(stationService).getStationsByIds(ids);
    }

    @Test
    void getStationsByIds_shouldReturnOk_whenBodyIsEmptyArray() throws Exception {
        // given
        when(stationService.getStationsByIds(List.of())).thenReturn(List.of());

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/internal/stations/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        verify(stationService).getStationsByIds(List.of());
    }

    @Test
    void getStationsByIds_shouldReject_whenBodyContainsNonUuid() throws Exception {
        // given
        String malformedJson = "[\"not-a-uuid\"]";

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/internal/stations/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(fuelPriceService, stationService);
    }

    @Test
    void getDetailsForTransaction_shouldReturnOk_whenStationIdIsUuid() throws Exception {
        // given
        UUID stationId = UUID.randomUUID();
        StationTransactionResponse body = mock(StationTransactionResponse.class);
        when(stationService.getDetailsForTransaction(stationId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/stations/{id}/details", stationId));

        // then
        result.andExpect(status().isOk());
        verify(stationService).getDetailsForTransaction(stationId);
    }

    @Test
    void getDetailsForTransaction_shouldThrow_whenStationIdIsNotUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                get("/api/v1/internal/stations/{id}/details", malformed)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(fuelPriceService, stationService);
    }

    @Test
    void validateStation_shouldReturnOk_whenStationIdIsUuid() throws Exception {
        // given
        UUID stationId = UUID.randomUUID();
        StationValidationResponse body = mock(StationValidationResponse.class);
        when(stationService.validateStation(stationId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/stations/{id}/validate", stationId));

        // then
        result.andExpect(status().isOk());
        verify(stationService).validateStation(stationId);
    }

    @Test
    void validateStation_shouldThrow_whenStationIdIsNotUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                get("/api/v1/internal/stations/{id}/validate", malformed)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(fuelPriceService, stationService);
    }

    @Test
    void getDashboardSummary_shouldReturnOk_whenCalled() throws Exception {
        // given
        StationDashboardSummaryResponse body = mock(StationDashboardSummaryResponse.class);
        when(stationService.getDashboardSummary()).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/stations/dashboard/summary"));

        // then
        result.andExpect(status().isOk());
        verify(stationService).getDashboardSummary();
    }
}