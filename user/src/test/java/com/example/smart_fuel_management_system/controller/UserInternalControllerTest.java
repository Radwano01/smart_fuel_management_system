package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.UserDashboardSummaryResponse;
import com.example.smart_fuel_management_system.dto.UserResponse;
import com.example.smart_fuel_management_system.dto.UserResponseToPaymentService;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import com.example.smart_fuel_management_system.security.JwtService;
import com.example.smart_fuel_management_system.service.UserService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserInternalController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserInternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    @Test
    void getUserResponse_shouldReturnOk_whenPathVariableIsUuid() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UserResponse body = mock(UserResponse.class);
        when(userService.getUserResponse(userId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/users/{id}", userId));

        // then
        result.andExpect(status().isOk());
        verify(userService).getUserResponse(userId);
    }

    @Test
    void getUserResponse_shouldThrow_whenPathVariableIsNotUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                get("/api/v1/internal/users/{id}", malformed)))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userService);
    }

    @Test
    void getUserForPayment_shouldReturnOk_whenPathVariableIsUuid() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UserResponseToPaymentService body = mock(UserResponseToPaymentService.class);
        when(userService.getUserForPayment(userId)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/users/{id}/payment", userId));

        // then
        result.andExpect(status().isOk());
        verify(userService).getUserForPayment(userId);
    }

    @Test
    void getUserForPayment_shouldReject_whenPathVariableIsNotUuid() throws Exception {
        // given
        String malformed = "not-a-uuid";

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/users/{id}/payment", malformed));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(userService);
    }

    @Test
    void getDashboardSummary_shouldReturnOk_whenCalled() throws Exception {
        // given
        UserDashboardSummaryResponse body = mock(UserDashboardSummaryResponse.class);
        when(userService.getDashboardSummary()).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/users/dashboard/summary"));

        // then
        result.andExpect(status().isOk());
        verify(userService).getDashboardSummary();
    }

    @Test
    void updateEmail_shouldReturnNoContent_whenPathVariableIsUuid() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        String email = "new@example.com";

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/internal/users/{id}/email", userId)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(email));

        // then
        result.andExpect(status().isNoContent());
        verify(userService).updateEmail(userId, email);
    }

    @Test
    void updateEmail_shouldThrow_whenPathVariableIsNotUuid() {
        // given
        String malformed = "not-a-uuid";

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                patch("/api/v1/internal/users/{id}/email", malformed)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("x@y.com")))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userService);
    }

    @Test
    void getUsersByIds_shouldReturnOk_whenBodyIsListOfUuids() throws Exception {
        // given
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        List<UserResponse> body = List.of(mock(UserResponse.class));
        String json = objectMapper.writeValueAsString(ids);
        when(userService.getUsersByIds(ids)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/internal/users/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
        verify(userService).getUsersByIds(ids);
    }

    @Test
    void getUsersByIds_shouldReturnOk_whenBodyIsEmptyArray() throws Exception {
        // given
        when(userService.getUsersByIds(List.of())).thenReturn(List.of());

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/internal/users/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        verify(userService).getUsersByIds(List.of());
    }

    @Test
    void getUsersByIds_shouldReject_whenBodyContainsNonUuid() throws Exception {
        // given
        String malformedJson = "[\"not-a-uuid\"]";

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/internal/users/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(userService);
    }

    @Test
    void getUsersByFullName_shouldReturnOk_whenFullNameParamProvided() throws Exception {
        // given
        String fullName = "Alice";
        List<UserResponse> body = List.of(mock(UserResponse.class));
        when(userService.getUsersByFullName(fullName)).thenReturn(body);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/users/search")
                        .param("fullName", fullName));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
        verify(userService).getUsersByFullName(fullName);
    }

    @Test
    void getUsersByFullName_shouldReject_whenFullNameParamMissing() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/internal/users/search"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(userService);
    }
}