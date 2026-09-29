package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.TransactionResponse;
import com.example.smart_fuel_management_system.dto.TransactionStatisticsResponse;
import com.example.smart_fuel_management_system.dto.TransactionSummaryResponse;
import com.example.smart_fuel_management_system.security.JwtService;
import com.example.smart_fuel_management_system.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JwtService jwtService;

    @Test
    void getStatistics_returnsOkWithServiceResult() throws Exception {
        // given
        LocalDate from = LocalDate.of(2026, 9, 1);
        LocalDate to = LocalDate.of(2026, 9, 10);
        UUID stationId = UUID.randomUUID();
        TransactionStatisticsResponse body = mock(TransactionStatisticsResponse.class);

        when(transactionService.getStatistics(from, to, List.of(stationId)))
                .thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/transactions/statistics")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .param("stationIds", stationId.toString()));

        // then
        result.andExpect(status().isOk());
        verify(transactionService).getStatistics(from, to, List.of(stationId));
    }

    @Test
    void getStatistics_acceptsMissingStationIds() throws Exception {
        // given
        LocalDate from = LocalDate.of(2026, 9, 1);
        LocalDate to = LocalDate.of(2026, 9, 10);
        TransactionStatisticsResponse body = mock(TransactionStatisticsResponse.class);

        when(transactionService.getStatistics(eq(from), eq(to), eq(null)))
                .thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/transactions/statistics")
                        .param("from", from.toString())
                        .param("to", to.toString()));

        // then
        result.andExpect(status().isOk());
        verify(transactionService).getStatistics(from, to, null);
    }

    @Test
    void getStatistics_rejectsMissingRequiredParams() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/transactions/statistics"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(transactionService);
    }

    @Test
    void getTransaction_returnsOkWithServiceResult() throws Exception {
        // given
        UUID transactionId = UUID.randomUUID();
        TransactionResponse body = mock(TransactionResponse.class);

        when(transactionService.getTransaction(transactionId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/transactions/{transactionId}", transactionId));

        // then
        result.andExpect(status().isOk());
        verify(transactionService).getTransaction(transactionId);
    }

    @Test
    void getUserTransactions_convertsUserIdAndDelegates() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        Pageable expected = PageRequest.of(0, 10);
        Page<TransactionResponse> page = Page.empty(expected);

        when(transactionService.getUserTransactions(userId, expected)).thenReturn(page);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/transactions/users/{userId}", userId)
                        .param("page", "0")
                        .param("size", "10"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
        verify(transactionService).getUserTransactions(userId, expected);
    }

    @Test
    void getUserTransactions_returnsServerErrorOnMalformedUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() ->
                mockMvc.perform(get("/api/v1/admin/transactions/users/{userId}", malformed)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(transactionService);
    }

    @Test
    void getTransactions_passesRangeAndPageableToService() throws Exception {
        // given
        LocalDateTime from = LocalDateTime.of(2026, 9, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 9, 10, 23, 59);
        Pageable expected = PageRequest.of(0, 10);
        Page<TransactionResponse> page = Page.empty(expected);

        when(transactionService.getTransactions(from, to, expected)).thenReturn(page);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/transactions")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .param("page", "0")
                        .param("size", "10"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
        verify(transactionService).getTransactions(from, to, expected);
    }

    @Test
    void getTransactions_acceptsMissingRange() throws Exception {
        // given
        Pageable expected = PageRequest.of(0, 20);
        Page<TransactionResponse> page = Page.empty(expected);

        when(transactionService.getTransactions(null, null, expected)).thenReturn(page);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/admin/transactions"));

        // then
        result.andExpect(status().isOk());
        verify(transactionService).getTransactions(null, null, expected);
    }

    @Test
    void getVehicleTransactionHistory_convertsVehicleIdAndDelegates() throws Exception {
        // given
        UUID vehicleId = UUID.randomUUID();
        List<TransactionSummaryResponse> body =
                List.of(mock(TransactionSummaryResponse.class));

        when(transactionService.getVehicleTransactionHistory(vehicleId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/transactions/vehicles/{vehicleId}", vehicleId));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
        verify(transactionService).getVehicleTransactionHistory(vehicleId);
    }

    @Test
    void getVehicleTransactionHistory_returnsServerErrorOnMalformedUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() ->
                mockMvc.perform(get("/api/v1/admin/transactions/vehicles/{vehicleId}", malformed)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(transactionService);
    }
}