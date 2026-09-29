package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.controller.pump.PumpAdminController;
import com.example.smart_fuel_management_system.dto.pump.CreatePumpRequest;
import com.example.smart_fuel_management_system.dto.pump.PumpResponse;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import com.example.smart_fuel_management_system.security.JwtService;
import com.example.smart_fuel_management_system.service.PumpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PumpAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class PumpAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PumpService pumpService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    @Test
    void create_shouldReturnCreated_whenRequestIsValid() throws Exception {
        // given
        UUID stationId = UUID.randomUUID();
        CreatePumpRequest request = new CreatePumpRequest(Collections.singleton(FuelType.GASOLINE));
        String json = objectMapper.writeValueAsString(request);
        PumpResponse body = mock(PumpResponse.class);
        when(pumpService.create(eq(stationId), any(CreatePumpRequest.class)))
                .thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/admin/stations/{stationId}/pumps", stationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        // then
        result.andExpect(status().isCreated());
        verify(pumpService).create(eq(stationId), any(CreatePumpRequest.class));
    }

    @Test
    void create_shouldThrow_whenStationIdIsNotUuid() throws Exception {
        // given
        String malformed = "not-a-uuid";
        CreatePumpRequest request = new CreatePumpRequest(Collections.singleton(FuelType.GASOLINE));
        String json = objectMapper.writeValueAsString(request);

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                post("/api/v1/admin/stations/{stationId}/pumps", malformed)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(pumpService);
    }

    @Test
    void create_shouldReject_whenBodyIsMalformed() throws Exception {
        // given
        UUID stationId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/admin/stations/{stationId}/pumps", stationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(pumpService);
    }

    @Test
    void create_shouldReject_whenBodyIsEmpty() throws Exception {
        // given
        UUID stationId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/admin/stations/{stationId}/pumps", stationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(pumpService);
    }
}