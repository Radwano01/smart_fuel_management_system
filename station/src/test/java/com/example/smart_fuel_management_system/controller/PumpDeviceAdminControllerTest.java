package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.controller.pump.PumpDeviceAdminController;
import com.example.smart_fuel_management_system.dto.pump.AssignPumpDeviceRequest;
import com.example.smart_fuel_management_system.dto.pump.PumpDeviceResponse;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import com.example.smart_fuel_management_system.security.JwtService;
import com.example.smart_fuel_management_system.service.PumpDeviceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PumpDeviceAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class PumpDeviceAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PumpDeviceService pumpDeviceService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    @Test
    void getByDeviceId_shouldReturnOk_whenDeviceIdProvided() throws Exception {
        // given
        String deviceId = "DEVICE-001";
        PumpDeviceResponse body = mock(PumpDeviceResponse.class);
        when(pumpDeviceService.getByDeviceId(deviceId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/pump-devices/{deviceId}", deviceId));

        // then
        result.andExpect(status().isOk());
        verify(pumpDeviceService).getByDeviceId(deviceId);
    }

    @Test
    void getByDeviceId_shouldAcceptArbitraryString_whenDeviceIdIsNotUuid() throws Exception {
        // given
        String deviceId = "not-a-uuid-but-a-device-code";
        PumpDeviceResponse body = mock(PumpDeviceResponse.class);
        when(pumpDeviceService.getByDeviceId(deviceId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/pump-devices/{deviceId}", deviceId));

        // then
        result.andExpect(status().isOk());
        verify(pumpDeviceService).getByDeviceId(deviceId);
    }

    @Test
    void assignDevice_shouldReturnOk_whenRequestIsValid() throws Exception {
        // given
        UUID pumpId = UUID.randomUUID();
        AssignPumpDeviceRequest request = new AssignPumpDeviceRequest("DEVICE-001");
        String json = objectMapper.writeValueAsString(request);
        PumpDeviceResponse body = mock(PumpDeviceResponse.class);
        when(pumpDeviceService.assignToPump(eq(pumpId), eq("DEVICE-001")))
                .thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                put("/api/v1/admin/pump-devices/{pumpId}/device", pumpId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        // then
        result.andExpect(status().isOk());
        verify(pumpDeviceService).assignToPump(eq(pumpId), eq("DEVICE-001"));
    }

    @Test
    void assignDevice_shouldReject_whenBodyIsMalformed() throws Exception {
        // given
        UUID pumpId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                put("/api/v1/admin/pump-devices/{pumpId}/device", pumpId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(pumpDeviceService);
    }

    @Test
    void assignDevice_shouldThrow_whenPumpIdIsNotUuid() throws Exception {
        // given
        String malformed = "not-a-uuid";
        AssignPumpDeviceRequest request = new AssignPumpDeviceRequest("DEVICE-001");
        String json = objectMapper.writeValueAsString(request);

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                put("/api/v1/admin/pump-devices/{pumpId}/device", malformed)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(pumpDeviceService);
    }

    @Test
    void unassignDevice_shouldReturnNoContent_whenPumpIdIsUuid() throws Exception {
        // given
        UUID pumpId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                delete("/api/v1/admin/pump-devices/{pumpId}/device", pumpId));

        // then
        result.andExpect(status().isNoContent());
        verify(pumpDeviceService).unassignFromPump(pumpId);
    }

    @Test
    void unassignDevice_shouldThrow_whenPumpIdIsNotUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                delete("/api/v1/admin/pump-devices/{pumpId}/device", malformed)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(pumpDeviceService);
    }
}