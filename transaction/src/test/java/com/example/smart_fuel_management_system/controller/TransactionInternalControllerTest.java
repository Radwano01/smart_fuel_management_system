package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.TransactionCountResponse;
import com.example.smart_fuel_management_system.dto.TransactionDashboardSummaryResponse;
import com.example.smart_fuel_management_system.dto.TransactionStationResponse;
import com.example.smart_fuel_management_system.security.JwtService;
import com.example.smart_fuel_management_system.service.TransactionService;
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

@WebMvcTest(TransactionInternalController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionInternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JwtService jwtService;

    @Test
    void getUserTransactionCount_returnsOkWithServiceResult() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        TransactionCountResponse body = mock(TransactionCountResponse.class);

        when(transactionService.getUserTransactionCount(userId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/transactions/users/{userId}", userId));

        // then
        result.andExpect(status().isOk());
        verify(transactionService).getUserTransactionCount(userId);
    }

    @Test
    void getUserTransactionCount_rejectsMalformedUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                get("/api/v1/internal/transactions/users/{userId}", malformed)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(transactionService);
    }

    @Test
    void getDashboardSummary_returnsOkWithServiceResult() throws Exception {
        // given
        TransactionDashboardSummaryResponse body =
                mock(TransactionDashboardSummaryResponse.class);

        when(transactionService.getDashboardSummary()).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/transactions/dashboard/summary"));

        // then
        result.andExpect(status().isOk());
        verify(transactionService).getDashboardSummary();
    }

    @Test
    void getStationTransactions_returnsOkWithServiceResult() throws Exception {
        // given
        UUID stationId = UUID.randomUUID();
        TransactionStationResponse body = mock(TransactionStationResponse.class);

        when(transactionService.getTransactionsCountByStationId(stationId))
                .thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/transactions/stations/{stationId}", stationId));

        // then
        result.andExpect(status().isOk());
        verify(transactionService).getTransactionsCountByStationId(stationId);
    }

    @Test
    void getStationTransactions_rejectsMalformedUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                get("/api/v1/internal/transactions/stations/{stationId}", malformed)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(transactionService);
    }

    @Test
    void getStationsTransactions_returnsOkWithServiceResult() throws Exception {
        // given
        List<UUID> stationIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        List<TransactionStationResponse> body =
                List.of(mock(TransactionStationResponse.class));
        String requestJson = objectMapper.writeValueAsString(stationIds);

        when(transactionService.getStationsTransactions(stationIds)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/internal/transactions/stations/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
        verify(transactionService).getStationsTransactions(stationIds);
    }

    @Test
    void getStationsTransactions_returnsEmptyListWhenServiceReturnsEmpty() throws Exception {
        // given
        List<UUID> stationIds = List.of(UUID.randomUUID());
        String requestJson = objectMapper.writeValueAsString(stationIds);

        when(transactionService.getStationsTransactions(stationIds)).thenReturn(List.of());

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/internal/transactions/stations/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        verify(transactionService).getStationsTransactions(stationIds);
    }

    @Test
    void getStationsTransactions_acceptsEmptyArray() throws Exception {
        // given
        String requestJson = "[]";

        when(transactionService.getStationsTransactions(List.of())).thenReturn(List.of());

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/internal/transactions/stations/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        verify(transactionService).getStationsTransactions(List.of());
    }

    @Test
    void getStationsTransactions_rejectsMalformedBody() throws Exception {
        // given
        String malformedJson = "[\"not-a-uuid\"]";

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/internal/transactions/stations/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(transactionService);
    }
}