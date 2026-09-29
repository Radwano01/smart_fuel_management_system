package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.TransactionDetailsResponse;
import com.example.smart_fuel_management_system.dto.TransactionSummaryResponse;
import com.example.smart_fuel_management_system.security.JwtService;
import com.example.smart_fuel_management_system.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JwtService jwtService;

    private Principal principalFor(UUID userId) {
        return () -> userId.toString();
    }

    @Test
    void getById_returnsOkWithServiceResult() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        TransactionDetailsResponse body = mock(TransactionDetailsResponse.class);

        when(transactionService.getById(transactionId, userId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/transactions/{id}", transactionId)
                        .principal(principalFor(userId)));

        // then
        result.andExpect(status().isOk());
        verify(transactionService).getById(transactionId, userId);
    }

    @Test
    void getById_rejectsMalformedTransactionId() {
        // given
        UUID userId = UUID.randomUUID();
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                get("/api/v1/transactions/{id}", malformed)
                        .principal(principalFor(userId))))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(transactionService);
    }

    @Test
    void getById_rejectsMalformedPrincipalName() {
        // given
        UUID transactionId = UUID.randomUUID();
        Principal malformedPrincipal = () -> "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                get("/api/v1/transactions/{id}", transactionId)
                        .principal(malformedPrincipal)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(transactionService);
    }

    @Test
    void getByUser_returnsOkWithServiceResult() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        List<TransactionSummaryResponse> body =
                List.of(mock(TransactionSummaryResponse.class));

        when(transactionService.getByUserId(userId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/transactions")
                        .principal(principalFor(userId)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
        verify(transactionService).getByUserId(userId);
    }

    @Test
    void getByUser_returnsEmptyListWhenUserHasNoTransactions() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        when(transactionService.getByUserId(userId)).thenReturn(List.of());

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/transactions")
                        .principal(principalFor(userId)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        verify(transactionService).getByUserId(userId);
    }

    @Test
    void getRecentTransactions_returnsOkWithServiceResult() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        List<TransactionSummaryResponse> body =
                List.of(mock(TransactionSummaryResponse.class));

        when(transactionService.recentTransactions(userId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/transactions/recent")
                        .principal(principalFor(userId)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
        verify(transactionService).recentTransactions(userId);
    }

    @Test
    void getRecentTransactions_returnsEmptyListWhenUserHasNoTransactions() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        when(transactionService.recentTransactions(userId)).thenReturn(List.of());

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/transactions/recent")
                        .principal(principalFor(userId)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        verify(transactionService).recentTransactions(userId);
    }
}