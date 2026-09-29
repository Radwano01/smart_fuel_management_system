package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.AddRequest;
import com.example.smart_fuel_management_system.dto.ListResponse;
import com.example.smart_fuel_management_system.dto.UpdateRequest;
import com.example.smart_fuel_management_system.dto.VehicleDTO;
import com.example.smart_fuel_management_system.enums.FuelType;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleControllerTest {

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

    private Authentication authFor(String name) {
        return new UsernamePasswordAuthenticationToken(
                name,
                null,
                List.of(new SimpleGrantedAuthority("USER"))
        );
    }

    // ---------------------------------------------------------------------
    // create
    // ---------------------------------------------------------------------

    @Test
    void create_shouldReturnCreated_whenRequestIsValid() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        AddRequest request = new AddRequest(
                "34ABC123", "Toyota", "Corolla", 2020, FuelType.GASOLINE);
        String json = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/vehicles")
                        .principal(authFor(userId.toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        // then
        result.andExpect(status().isCreated());
        verify(vehicleService).create(eq(userId), any(AddRequest.class));
    }

    @Test
    void create_shouldReject_whenBodyIsMalformed() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/vehicles")
                        .principal(authFor(userId.toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(vehicleService);
    }

    @Test
    void create_shouldThrow_whenAuthenticationNameIsNotUuid() {
        // given
        AddRequest request = new AddRequest(
                "34ABC123", "Toyota", "Corolla", 2020, FuelType.GASOLINE);
        String json;
        try {
            json = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String malformedPrincipal = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                post("/api/v1/vehicles")
                        .principal(authFor(malformedPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(vehicleService);
    }

    // ---------------------------------------------------------------------
    // list
    // ---------------------------------------------------------------------

    @Test
    void list_shouldReturnOk_whenAuthenticationNameIsUuid() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        List<ListResponse> body = List.of(mock(ListResponse.class));
        when(vehicleService.findAllByUserId(userId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/vehicles")
                        .principal(authFor(userId.toString())));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
        verify(vehicleService).findAllByUserId(userId);
    }

    @Test
    void list_shouldThrow_whenAuthenticationNameIsNotUuid() {
        // given
        String malformedPrincipal = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                get("/api/v1/vehicles")
                        .principal(authFor(malformedPrincipal))))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(vehicleService);
    }

    // ---------------------------------------------------------------------
    // getById
    // ---------------------------------------------------------------------

    @Test
    void getById_shouldReturnOk_whenBothIdsAreUuid() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        VehicleDTO body = mock(VehicleDTO.class);
        when(vehicleService.findByUserIdAndVehicleId(userId, vehicleId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/vehicles/{vehicleId}", vehicleId)
                        .principal(authFor(userId.toString())));

        // then
        result.andExpect(status().isOk());
        verify(vehicleService).findByUserIdAndVehicleId(userId, vehicleId);
    }

    @Test
    void getById_shouldReject_whenPathVariableIsNotUuid() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/vehicles/{vehicleId}", "not-a-uuid")
                        .principal(authFor(userId.toString())));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(vehicleService);
    }

    @Test
    void getById_shouldThrow_whenAuthenticationNameIsNotUuid() {
        // given
        UUID vehicleId = UUID.randomUUID();
        String malformedPrincipal = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                get("/api/v1/vehicles/{vehicleId}", vehicleId)
                        .principal(authFor(malformedPrincipal))))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(vehicleService);
    }

    // ---------------------------------------------------------------------
    // update
    // ---------------------------------------------------------------------

    @Test
    void update_shouldReturnNoContent_whenBothIdsAreUuid() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UpdateRequest request = new UpdateRequest("Honda", "Civic", FuelType.DIESEL);
        String json = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/vehicles/{vehicleId}", vehicleId)
                        .principal(authFor(userId.toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        // then
        result.andExpect(status().isNoContent());
        verify(vehicleService).update(eq(userId), eq(vehicleId), any(UpdateRequest.class));
    }

    @Test
    void update_shouldReject_whenBodyIsMalformed() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/vehicles/{vehicleId}", vehicleId)
                        .principal(authFor(userId.toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(vehicleService);
    }

    @Test
    void update_shouldReject_whenPathVariableIsNotUuid() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UpdateRequest request = new UpdateRequest("Honda", "Civic", FuelType.DIESEL);
        String json = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/vehicles/{vehicleId}", "not-a-uuid")
                        .principal(authFor(userId.toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(vehicleService);
    }

    @Test
    void update_shouldThrow_whenAuthenticationNameIsNotUuid() {
        // given
        UUID vehicleId = UUID.randomUUID();
        String malformedPrincipal = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                patch("/api/v1/vehicles/{vehicleId}", vehicleId)
                        .principal(authFor(malformedPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(vehicleService);
    }

    // ---------------------------------------------------------------------
    // deactivate
    // ---------------------------------------------------------------------

    @Test
    void deactivate_shouldReturnNoContent_whenBothIdsAreUuid() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/vehicles/{vehicleId}/deactivate", vehicleId)
                        .principal(authFor(userId.toString())));

        // then
        result.andExpect(status().isNoContent());
        verify(vehicleService).deactivate(userId, vehicleId);
    }

    @Test
    void deactivate_shouldReject_whenPathVariableIsNotUuid() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/vehicles/{vehicleId}/deactivate", "not-a-uuid")
                        .principal(authFor(userId.toString())));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(vehicleService);
    }

    @Test
    void deactivate_shouldThrow_whenAuthenticationNameIsNotUuid() {
        // given
        UUID vehicleId = UUID.randomUUID();
        String malformedPrincipal = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                patch("/api/v1/vehicles/{vehicleId}/deactivate", vehicleId)
                        .principal(authFor(malformedPrincipal))))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(vehicleService);
    }

    // ---------------------------------------------------------------------
    // activate
    // ---------------------------------------------------------------------

    @Test
    void activate_shouldReturnNoContent_whenBothIdsAreUuid() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/vehicles/{vehicleId}/activate", vehicleId)
                        .principal(authFor(userId.toString())));

        // then
        result.andExpect(status().isNoContent());
        verify(vehicleService).activate(userId, vehicleId);
    }

    @Test
    void activate_shouldReject_whenPathVariableIsNotUuid() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/vehicles/{vehicleId}/activate", "not-a-uuid")
                        .principal(authFor(userId.toString())));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(vehicleService);
    }

    @Test
    void activate_shouldThrow_whenAuthenticationNameIsNotUuid() {
        // given
        UUID vehicleId = UUID.randomUUID();
        String malformedPrincipal = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                patch("/api/v1/vehicles/{vehicleId}/activate", vehicleId)
                        .principal(authFor(malformedPrincipal))))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(vehicleService);
    }
}