package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.VehicleCountResponse;
import com.example.smart_fuel_management_system.dto.VehicleDashboardSummaryResponse;
import com.example.smart_fuel_management_system.dto.VehicleResponse;
import com.example.smart_fuel_management_system.dto.VehicleTransactionResponse;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import com.example.smart_fuel_management_system.security.JwtService;
import com.example.smart_fuel_management_system.service.VehicleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleInternalController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleInternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VehicleService vehicleService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    // ---------------------------------------------------------------------
    // resolve
    // ---------------------------------------------------------------------

    @Test
    void resolve_shouldReturnOk_whenBothParamsProvided() throws Exception {
        // given
        String rfidTag = "RFID-1";
        String plateNumber = "34ABC123";
        VehicleResponse body = mock(VehicleResponse.class);
        when(vehicleService.resolve(rfidTag, plateNumber)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/vehicles/resolve")
                        .param("rfidTag", rfidTag)
                        .param("plateNumber", plateNumber));

        // then
        result.andExpect(status().isOk());
        verify(vehicleService).resolve(rfidTag, plateNumber);
    }

    @Test
    void resolve_shouldReject_whenRfidTagParamMissing() throws Exception {
        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/vehicles/resolve")
                        .param("plateNumber", "34ABC123"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(vehicleService);
    }

    @Test
    void resolve_shouldReject_whenPlateNumberParamMissing() throws Exception {
        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/vehicles/resolve")
                        .param("rfidTag", "RFID-1"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(vehicleService);
    }

    // ---------------------------------------------------------------------
    // getUserVehicleCount
    // ---------------------------------------------------------------------

    @Test
    void getUserVehicleCount_shouldReturnOk_whenPathVariableIsUuid() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        VehicleCountResponse body = mock(VehicleCountResponse.class);
        when(vehicleService.getVehicleCount(userId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/vehicles/users/{userId}", userId));

        // then
        result.andExpect(status().isOk());
        verify(vehicleService).getVehicleCount(userId);
    }

    @Test
    void getUserVehicleCount_shouldReject_whenPathVariableIsNotUuid() throws Exception {
        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/vehicles/users/{userId}", "not-a-uuid"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(vehicleService);
    }

    // ---------------------------------------------------------------------
    // getVehicleSummaryDetails
    // ---------------------------------------------------------------------

    @Test
    void getVehicleSummaryDetails_shouldReturnOk_whenPathVariableIsUuid() throws Exception {
        // given
        UUID vehicleId = UUID.randomUUID();
        VehicleTransactionResponse body = mock(VehicleTransactionResponse.class);
        when(vehicleService.getVehicleSummaryDetails(vehicleId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/vehicles/{id}", vehicleId));

        // then
        result.andExpect(status().isOk());
        verify(vehicleService).getVehicleSummaryDetails(vehicleId);
    }

    @Test
    void getVehicleSummaryDetails_shouldThrow_whenPathVariableIsNotUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                get("/api/v1/internal/vehicles/{id}", malformed)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(vehicleService);
    }

    // ---------------------------------------------------------------------
    // getDashboardSummary
    // ---------------------------------------------------------------------

    @Test
    void getDashboardSummary_shouldReturnOk_whenCalled() throws Exception {
        // given
        VehicleDashboardSummaryResponse body = mock(VehicleDashboardSummaryResponse.class);
        when(vehicleService.getDashboardSummary()).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/vehicles/dashboard/summary"));

        // then
        result.andExpect(status().isOk());
        verify(vehicleService).getDashboardSummary();
    }

    // ---------------------------------------------------------------------
    // getVehiclesByIds
    // ---------------------------------------------------------------------

    @Test
    void getVehiclesByIds_shouldReturnOk_whenBodyIsListOfUuids() throws Exception {
        // given
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        List<VehicleTransactionResponse> body = List.of(mock(VehicleTransactionResponse.class));
        String json = objectMapper.writeValueAsString(ids);
        when(vehicleService.getVehiclesByIds(ids)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/internal/vehicles/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
        verify(vehicleService).getVehiclesByIds(ids);
    }

    @Test
    void getVehiclesByIds_shouldReturnOk_whenBodyIsEmptyArray() throws Exception {
        // given
        when(vehicleService.getVehiclesByIds(List.of())).thenReturn(List.of());

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/internal/vehicles/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        verify(vehicleService).getVehiclesByIds(List.of());
    }

    @Test
    void getVehiclesByIds_shouldReject_whenBodyContainsNonUuid() throws Exception {
        // given
        String malformedJson = "[\"not-a-uuid\"]";

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/internal/vehicles/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(vehicleService);
    }

    @Test
    void getVehiclesByIds_shouldReject_whenBodyIsMalformedJson() throws Exception {
        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/internal/vehicles/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(vehicleService);
    }
}