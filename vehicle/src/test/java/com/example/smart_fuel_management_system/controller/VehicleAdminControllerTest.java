package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.ListResponse;
import com.example.smart_fuel_management_system.dto.VehicleDTO;
import com.example.smart_fuel_management_system.dto.VehicleStatusRequest;
import com.example.smart_fuel_management_system.enums.VehicleStatusType;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import com.example.smart_fuel_management_system.security.JwtService;
import com.example.smart_fuel_management_system.service.VehicleAdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VehicleAdminService adminVehicleService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    @Test
    void changeStatus_shouldReturnNoContent_whenRequestIsValid() throws Exception {
        // given
        UUID vehicleId = UUID.randomUUID();
        VehicleStatusRequest request = new VehicleStatusRequest(VehicleStatusType.ACTIVE);
        String json = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/admin/vehicles/{vehicleId}/status", vehicleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        // then
        result.andExpect(status().isNoContent());
        verify(adminVehicleService).changeStatus(vehicleId, VehicleStatusType.ACTIVE);
    }

    @Test
    void changeStatus_shouldReject_whenBodyIsMalformed() throws Exception {
        // given
        UUID vehicleId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/admin/vehicles/{vehicleId}/status", vehicleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(adminVehicleService);
    }

    @Test
    void changeStatus_shouldReject_whenPathVariableIsNotUuid() throws Exception {
        // given
        VehicleStatusRequest request = new VehicleStatusRequest(VehicleStatusType.ACTIVE);
        String json = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/admin/vehicles/{vehicleId}/status", "not-a-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(adminVehicleService);
    }

    @Test
    void delete_shouldReturnNoContent_whenPathVariableIsUuid() throws Exception {
        // given
        UUID vehicleId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                delete("/api/v1/admin/vehicles/{vehicleId}", vehicleId));

        // then
        result.andExpect(status().isNoContent());
        verify(adminVehicleService).delete(vehicleId);
    }

    @Test
    void delete_shouldReject_whenPathVariableIsNotUuid() throws Exception {
        // when
        ResultActions result = mockMvc.perform(
                delete("/api/v1/admin/vehicles/{vehicleId}", "not-a-uuid"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(adminVehicleService);
    }

    @Test
    void findByPlateNumber_shouldReturnOk_whenPlateExists() throws Exception {
        // given
        String plateNumber = "34ABC123";
        VehicleDTO body = mock(VehicleDTO.class);
        when(adminVehicleService.findByPlateNumber(plateNumber)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/vehicles/plates/{plateNumber}", plateNumber));

        // then
        result.andExpect(status().isOk());
        verify(adminVehicleService).findByPlateNumber(plateNumber);
    }

    @Test
    void getUserVehicles_shouldReturnOk_whenPathVariableIsUuid() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        List<VehicleDTO> body = List.of(mock(VehicleDTO.class));
        when(adminVehicleService.getUserVehicles(userId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/vehicles/users/{userId}", userId));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
        verify(adminVehicleService).getUserVehicles(userId);
    }

    @Test
    void getUserVehicles_shouldThrow_whenPathVariableIsNotUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                get("/api/v1/admin/vehicles/users/{userId}", malformed)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(adminVehicleService);
    }

    @Test
    void getVehicles_shouldReturnOkWithDefaultPage_whenNoPageParamsProvided() throws Exception {
        // given
        Pageable expected = PageRequest.of(0, 20);
        Page<ListResponse> page = new PageImpl<>(List.of(), expected, 0);
        when(adminVehicleService.getVehicles(expected)).thenReturn(page);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/vehicles"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
        verify(adminVehicleService).getVehicles(expected);
    }

    @Test
    void getVehicles_shouldPassPaginationToService_whenPageParamsProvided() throws Exception {
        // given
        Pageable expected = PageRequest.of(2, 5);
        Page<ListResponse> page = new PageImpl<>(List.of(), expected, 0);
        when(adminVehicleService.getVehicles(expected)).thenReturn(page);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/vehicles")
                        .param("page", "2")
                        .param("size", "5"));

        // then
        result.andExpect(status().isOk());
        verify(adminVehicleService).getVehicles(expected);
    }

    @Test
    void getVehicleDetails_shouldReturnOk_whenPathVariableIsUuid() throws Exception {
        // given
        UUID vehicleId = UUID.randomUUID();
        VehicleDTO body = mock(VehicleDTO.class);
        when(adminVehicleService.getVehicleDetails(vehicleId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/vehicles/{vehicleId}", vehicleId));

        // then
        result.andExpect(status().isOk());
        verify(adminVehicleService).getVehicleDetails(vehicleId);
    }

    @Test
    void getVehicleDetails_shouldReject_whenPathVariableIsNotUuid() throws Exception {
        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/vehicles/{vehicleId}", "not-a-uuid"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(adminVehicleService);
    }
}