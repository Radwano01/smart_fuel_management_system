package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.controller.station.StationAdminController;
import com.example.smart_fuel_management_system.dto.station.CreateStationRequest;
import com.example.smart_fuel_management_system.dto.station.StationDetailsResponse;
import com.example.smart_fuel_management_system.dto.station.StationSummaryResponse;
import com.example.smart_fuel_management_system.dto.station.UpdateStationRequest;
import com.example.smart_fuel_management_system.enums.StationStatusType;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import com.example.smart_fuel_management_system.security.JwtService;
import com.example.smart_fuel_management_system.service.StationService;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
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

@WebMvcTest(StationAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class StationAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StationService stationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    @Test
    void create_shouldReturnCreated_whenRequestIsValid() throws Exception {
        // given
        CreateStationRequest request = new CreateStationRequest(
                "Central Station",
                "Istanbul",
                "100 Main Street",
                "+90 555 000 0000",
                new BigDecimal("41.0082"),
                new BigDecimal("28.9784"));
        String json = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/admin/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        // then
        result.andExpect(status().isCreated());
        verify(stationService).create(any(CreateStationRequest.class));
    }

    @Test
    void create_shouldReject_whenBodyIsMalformed() throws Exception {
        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/admin/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(stationService);
    }

    @Test
    void create_shouldReject_whenRequiredFieldsAreMissing() throws Exception {
        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/admin/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(stationService);
    }

    @Test
    void getStationDetails_shouldReturnOk_whenStationIdIsUuid() throws Exception {
        // given
        UUID stationId = UUID.randomUUID();
        StationDetailsResponse body = mock(StationDetailsResponse.class);
        when(stationService.getStationDetails(stationId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/stations/{stationId}", stationId));

        // then
        result.andExpect(status().isOk());
        verify(stationService).getStationDetails(stationId);
    }

    @Test
    void getStationDetails_shouldThrow_whenStationIdIsNotUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                get("/api/v1/admin/stations/{stationId}", malformed)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(stationService);
    }

    @Test
    void updateStation_shouldReturnNoContent_whenRequestIsValid() throws Exception {
        // given
        UUID stationId = UUID.randomUUID();
        UpdateStationRequest request = UpdateStationRequest.builder()
                .name("Renamed Station")
                .address("200 New Street")
                .city("Ankara")
                .contactInformation("+90 555 111 1111")
                .latitude(new BigDecimal("39.9334"))
                .longitude(new BigDecimal("32.8597"))
                .status(StationStatusType.ACTIVE)
                .build();
        String json = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/admin/stations/{stationId}", stationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        // then
        result.andExpect(status().isNoContent());
        verify(stationService).updateStation(eq(stationId), any(UpdateStationRequest.class));
    }

    @Test
    void updateStation_shouldReject_whenBodyIsMalformed() throws Exception {
        // given
        UUID stationId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/admin/stations/{stationId}", stationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(stationService);
    }

    @Test
    void updateStation_shouldThrow_whenStationIdIsNotUuid() {
        // given
        String malformed = "not-a-uuid";
        String json = "{}";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                patch("/api/v1/admin/stations/{stationId}", malformed)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(stationService);
    }

    @Test
    void getStations_shouldReturnOkWithDefaultPaging_whenNoParamsProvided() throws Exception {
        // given
        Pageable expected = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StationSummaryResponse> page = new PageImpl<>(List.of(), expected, 0);
        when(stationService.searchAndFilterStations(null, null, expected)).thenReturn(page);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/stations"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
        verify(stationService).searchAndFilterStations(null, null, expected);
    }

    @Test
    void getStations_shouldPassSearchAndStatusToService_whenProvided() throws Exception {
        // given
        Pageable expected = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StationSummaryResponse> page = new PageImpl<>(List.of(), expected, 0);
        when(stationService.searchAndFilterStations(
                "Central", StationStatusType.ACTIVE, expected)).thenReturn(page);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/stations")
                        .param("search", "Central")
                        .param("status", "ACTIVE"));

        // then
        result.andExpect(status().isOk());
        verify(stationService).searchAndFilterStations(
                "Central", StationStatusType.ACTIVE, expected);
    }

    @Test
    void getStations_shouldPassPageAndSizeToService_whenProvided() throws Exception {
        // given
        Pageable expected = PageRequest.of(3, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StationSummaryResponse> page = new PageImpl<>(List.of(), expected, 0);
        when(stationService.searchAndFilterStations(null, null, expected)).thenReturn(page);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/stations")
                        .param("page", "3")
                        .param("size", "5"));

        // then
        result.andExpect(status().isOk());
        verify(stationService).searchAndFilterStations(null, null, expected);
    }

    @Test
    void getStations_shouldReject_whenStatusIsInvalidEnum() throws Exception {
        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/stations")
                        .param("status", "NOT_A_STATUS"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(stationService);
    }
}