package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.controller.fuelPrice.FuelPriceAdminController;
import com.example.smart_fuel_management_system.dto.fuelPrice.FuelPriceRequest;
import com.example.smart_fuel_management_system.dto.fuelPrice.FuelPriceResponse;
import com.example.smart_fuel_management_system.enums.FuelPriceStatusType;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import com.example.smart_fuel_management_system.security.JwtService;
import com.example.smart_fuel_management_system.service.FuelPriceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FuelPriceAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class FuelPriceAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FuelPriceService fuelPriceService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    @Test
    void create_shouldReturnCreated_whenRequestIsValid() throws Exception {
        // given
        UUID stationId = UUID.randomUUID();
        FuelPriceRequest request = new FuelPriceRequest(new BigDecimal("50.00"));
        String json = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/admin/stations/{stationId}/fuel-prices/{fuelType}",
                        stationId, "GASOLINE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        // then
        result.andExpect(status().isCreated());
        verify(fuelPriceService).create(eq(stationId), eq(FuelType.GASOLINE), any(FuelPriceRequest.class));
    }

    @Test
    void create_shouldThrow_whenStationIdIsNotUuid() {
        // given
        String malformed = "not-a-uuid";
        String json = "{\"price\":50.00}";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                post("/api/v1/admin/stations/{stationId}/fuel-prices/{fuelType}",
                        malformed, "GASOLINE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(fuelPriceService);
    }

    @Test
    void create_shouldReject_whenFuelTypeIsInvalid() throws Exception {
        // given
        UUID stationId = UUID.randomUUID();
        String json = "{\"price\":50.00}";

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/admin/stations/{stationId}/fuel-prices/{fuelType}",
                        stationId, "NOT_A_FUEL")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(fuelPriceService);
    }

    @Test
    void create_shouldReject_whenBodyIsMalformed() throws Exception {
        // given
        UUID stationId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/admin/stations/{stationId}/fuel-prices/{fuelType}",
                        stationId, "GASOLINE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(fuelPriceService);
    }

    @Test
    void getHistory_shouldReturnOk_whenStationIdIsUuid() throws Exception {
        // given
        UUID stationId = UUID.randomUUID();
        List<FuelPriceResponse> body = List.of(mock(FuelPriceResponse.class));
        when(fuelPriceService.getStationHistoryPrices(stationId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/stations/{stationId}/fuel-prices/history", stationId));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
        verify(fuelPriceService).getStationHistoryPrices(stationId);
    }

    @Test
    void getHistory_shouldReturnOk_whenHistoryIsEmpty() throws Exception {
        // given
        UUID stationId = UUID.randomUUID();
        when(fuelPriceService.getStationHistoryPrices(stationId)).thenReturn(List.of());

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/stations/{stationId}/fuel-prices/history", stationId));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        verify(fuelPriceService).getStationHistoryPrices(stationId);
    }

    @Test
    void getHistory_shouldThrow_whenStationIdIsNotUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                get("/api/v1/admin/stations/{stationId}/fuel-prices/history", malformed)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(fuelPriceService);
    }

    @Test
    void searchStationPrices_shouldReturnOk_whenNoFiltersProvided() throws Exception {
        // given
        UUID stationId = UUID.randomUUID();
        Pageable expected = PageRequest.of(0, 20);
        Page<FuelPriceResponse> page = Page.empty(expected);
        when(fuelPriceService.searchStationPrices(stationId, null, null, expected))
                .thenReturn(page);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/stations/{stationId}/fuel-prices", stationId));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
        verify(fuelPriceService).searchStationPrices(stationId, null, null, expected);
    }

    @Test
    void searchStationPrices_shouldPassFiltersToService_whenProvided() throws Exception {
        // given
        UUID stationId = UUID.randomUUID();
        Pageable expected = PageRequest.of(0, 20);
        Page<FuelPriceResponse> page = Page.empty(expected);
        when(fuelPriceService.searchStationPrices(
                stationId, FuelType.GASOLINE, FuelPriceStatusType.ACTIVE, expected))
                .thenReturn(page);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/stations/{stationId}/fuel-prices", stationId)
                        .param("fuelType", "GASOLINE")
                        .param("status", "ACTIVE"));

        // then
        result.andExpect(status().isOk());
        verify(fuelPriceService).searchStationPrices(
                stationId, FuelType.GASOLINE, FuelPriceStatusType.ACTIVE, expected);
    }

    @Test
    void searchStationPrices_shouldPassPaginationToService_whenPageParamsProvided() throws Exception {
        // given
        UUID stationId = UUID.randomUUID();
        Pageable expected = PageRequest.of(2, 5);
        Page<FuelPriceResponse> page = Page.empty(expected);
        when(fuelPriceService.searchStationPrices(stationId, null, null, expected))
                .thenReturn(page);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/stations/{stationId}/fuel-prices", stationId)
                        .param("page", "2")
                        .param("size", "5"));

        // then
        result.andExpect(status().isOk());
        verify(fuelPriceService).searchStationPrices(stationId, null, null, expected);
    }

    @Test
    void searchStationPrices_shouldThrow_whenStationIdIsNotUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                get("/api/v1/admin/stations/{stationId}/fuel-prices", malformed)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(fuelPriceService);
    }
}