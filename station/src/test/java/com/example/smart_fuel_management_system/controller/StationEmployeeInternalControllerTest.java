package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.controller.stationEmployee.StationEmployeeInternalController;
import com.example.smart_fuel_management_system.dto.station.StationEmployeeAuthResponse;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import com.example.smart_fuel_management_system.security.JwtService;
import com.example.smart_fuel_management_system.service.StationEmployeeService;
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

@WebMvcTest(StationEmployeeInternalController.class)
@AutoConfigureMockMvc(addFilters = false)
class StationEmployeeInternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StationEmployeeService stationEmployeeService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    @Test
    void getStationDetails_shouldReturnOk_whenEmployeeIdIsUuid() throws Exception {
        // given
        UUID employeeId = UUID.randomUUID();
        StationEmployeeAuthResponse body = mock(StationEmployeeAuthResponse.class);
        when(stationEmployeeService.getStationDetails(employeeId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/station-employees/{employeeId}", employeeId));

        // then
        result.andExpect(status().isOk());
        verify(stationEmployeeService).getStationDetails(employeeId);
    }

    @Test
    void getStationDetails_shouldThrow_whenEmployeeIdIsNotUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                get("/api/v1/internal/station-employees/{employeeId}", malformed)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(stationEmployeeService);
    }
}