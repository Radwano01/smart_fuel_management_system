package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.AssignRfidRequest;
import com.example.smart_fuel_management_system.dto.VehicleDTO;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import com.example.smart_fuel_management_system.security.JwtService;
import com.example.smart_fuel_management_system.service.VehicleRfidService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleAdminRfidController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleAdminRfidControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VehicleRfidService vehicleRfidService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    @Test
    void assignRfid_shouldReturnNoContent_whenRequestIsValid() throws Exception {
        // given
        UUID vehicleId = UUID.randomUUID();
        AssignRfidRequest request = new AssignRfidRequest("RFID-NEW");
        String json = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/admin/vehicles/{vehicleId}/rfid", vehicleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        // then
        result.andExpect(status().isNoContent());
        verify(vehicleRfidService).assignRfid(eq(vehicleId), eq("RFID-NEW"));
    }

    @Test
    void assignRfid_shouldReject_whenBodyIsMalformed() throws Exception {
        // given
        UUID vehicleId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/admin/vehicles/{vehicleId}/rfid", vehicleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(vehicleRfidService);
    }

    @Test
    void assignRfid_shouldReject_whenPathVariableIsNotUuid() throws Exception {
        // given
        AssignRfidRequest request = new AssignRfidRequest("RFID-NEW");
        String json = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/admin/vehicles/{vehicleId}/rfid", "not-a-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(vehicleRfidService);
    }

    @Test
    void findByRfid_shouldReturnOk_whenRfidProvided() throws Exception {
        // given
        String rfid = "RFID-1";
        VehicleDTO body = mock(VehicleDTO.class);
        when(vehicleRfidService.findByRfid(rfid)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/vehicles/rfid/{rfid}", rfid));

        // then
        result.andExpect(status().isOk());
        verify(vehicleRfidService).findByRfid(rfid);
    }

    @Test
    void removeRfid_shouldReturnNoContent_whenPathVariableIsUuid() throws Exception {
        // given
        UUID vehicleId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                delete("/api/v1/admin/vehicles/{vehicleId}/rfid", vehicleId));

        // then
        result.andExpect(status().isNoContent());
        verify(vehicleRfidService).removeRfid(vehicleId);
    }

    @Test
    void removeRfid_shouldReject_whenPathVariableIsNotUuid() throws Exception {
        // when
        ResultActions result = mockMvc.perform(
                delete("/api/v1/admin/vehicles/{vehicleId}/rfid", "not-a-uuid"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(vehicleRfidService);
    }
}