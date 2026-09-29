package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.controller.pump.PumpInternalController;
import com.example.smart_fuel_management_system.dto.station.StationFuelSessionResponse;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import com.example.smart_fuel_management_system.security.JwtService;
import com.example.smart_fuel_management_system.service.PumpService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PumpInternalController.class)
@AutoConfigureMockMvc(addFilters = false)
class PumpInternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PumpService pumpService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    @Test
    void getStationId_shouldReturnOk_whenPumpIdIsUuid() throws Exception {
        // given
        UUID pumpId = UUID.randomUUID();
        StationFuelSessionResponse body = mock(StationFuelSessionResponse.class);
        when(pumpService.getStationId(pumpId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/stations/pumps/{pumpId}", pumpId));

        // then
        result.andExpect(status().isOk());
        verify(pumpService).getStationId(pumpId);
    }

    @Test
    void getStationId_shouldThrow_whenPumpIdIsNotUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                get("/api/v1/internal/stations/pumps/{pumpId}", malformed)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(pumpService);
    }
}