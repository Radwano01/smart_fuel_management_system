package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.controller.stationEmployee.StationEmployeeAdminController;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StationEmployeeAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class StationEmployeeAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StationEmployeeService stationEmployeeService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    @Test
    void changeStation_shouldReturnNoContent_whenBothIdsAreUuid() throws Exception {
        // given
        UUID employeeId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/admin/stations/{stationId}/employees/{employeeId}",
                        stationId, employeeId));

        // then
        result.andExpect(status().isNoContent());
        verify(stationEmployeeService).changeStation(employeeId, stationId);
    }

    @Test
    void changeStation_shouldThrow_whenEmployeeIdIsNotUuid() {
        // given
        String malformedEmployeeId = "not-a-uuid";
        UUID stationId = UUID.randomUUID();

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                patch("/api/v1/admin/stations/{stationId}/employees/{employeeId}",
                        stationId, malformedEmployeeId)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(stationEmployeeService);
    }

    @Test
    void changeStation_shouldThrow_whenStationIdIsNotUuid() {
        // given
        UUID employeeId = UUID.randomUUID();
        String malformedStationId = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                patch("/api/v1/admin/stations/{stationId}/employees/{employeeId}",
                        malformedStationId, employeeId)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(stationEmployeeService);
    }

    @Test
    void changeStation_shouldThrow_whenBothIdsAreNotUuid() {
        // given
        String malformedEmployeeId = "not-a-uuid";
        String malformedStationId = "also-not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                patch("/api/v1/admin/stations/{stationId}/employees/{employeeId}",
                        malformedStationId, malformedEmployeeId)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(stationEmployeeService);
    }

    @Test
    void removeStation_shouldReturnNoContent_whenStationIdIsUuid() throws Exception {
        // given
        UUID stationId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/admin/stations/{stationId}/employees", stationId));

        // then
        result.andExpect(status().isNoContent());
        verify(stationEmployeeService).removeStation(stationId);
    }

    @Test
    void removeStation_shouldThrow_whenStationIdIsNotUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                patch("/api/v1/admin/stations/{stationId}/employees", malformed)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(stationEmployeeService);
    }
}