package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.controller.station.StationController;
import com.example.smart_fuel_management_system.dto.station.StationResponse;
import com.example.smart_fuel_management_system.enums.StationStatusType;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import com.example.smart_fuel_management_system.security.JwtService;
import com.example.smart_fuel_management_system.service.StationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StationController.class)
@AutoConfigureMockMvc(addFilters = false)
class StationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StationService stationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    private Authentication authFor(String name) {
        return new UsernamePasswordAuthenticationToken(
                name,
                null,
                List.of(new SimpleGrantedAuthority("STATION"))
        );
    }

    @Test
    void getDetailsForStation_shouldReturnOk_whenPrincipalNameIsUuid() throws Exception {
        // given
        UUID employeeId = UUID.randomUUID();
        StationResponse body = mock(StationResponse.class);
        when(stationService.getDetailsForStation(employeeId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/stations")
                        .principal(authFor(employeeId.toString())));

        // then
        result.andExpect(status().isOk());
        verify(stationService).getDetailsForStation(employeeId);
    }

    @Test
    void getDetailsForStation_shouldThrow_whenPrincipalNameIsNotUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                get("/api/v1/stations")
                        .principal(authFor(malformed))))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(stationService);
    }

    @Test
    void changeStatus_shouldReturnNoContent_whenStatusAndPrincipalAreValid() throws Exception {
        // given
        UUID employeeId = UUID.randomUUID();
        StationStatusType status = StationStatusType.ACTIVE;

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/stations/status")
                        .param("status", status.name())
                        .principal(authFor(employeeId.toString())));

        // then
        result.andExpect(status().isNoContent());
        verify(stationService).changeStatus(status, employeeId);
    }

    @Test
    void changeStatus_shouldReject_whenStatusIsInvalidEnum() throws Exception {
        // given
        UUID employeeId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/stations/status")
                        .param("status", "NOT_A_STATUS")
                        .principal(authFor(employeeId.toString())));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(stationService);
    }

    @Test
    void changeStatus_shouldReject_whenStatusParamMissing() throws Exception {
        // given
        UUID employeeId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/stations/status")
                        .principal(authFor(employeeId.toString())));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(stationService);
    }

    @Test
    void changeStatus_shouldThrow_whenPrincipalNameIsNotUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                patch("/api/v1/stations/status")
                        .param("status", "ACTIVE")
                        .principal(authFor(malformed))))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(stationService);
    }
}