package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.UpdateDTO;
import com.example.smart_fuel_management_system.dto.UserSummaryResponse;
import com.example.smart_fuel_management_system.enums.AccountSearchType;
import com.example.smart_fuel_management_system.enums.AccountStatusType;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import com.example.smart_fuel_management_system.security.JwtService;
import com.example.smart_fuel_management_system.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserAdminControllerTest {

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

    private Authentication authFor(String name) {
        return new UsernamePasswordAuthenticationToken(
                name,
                null,
                List.of(new SimpleGrantedAuthority("ADMIN"))
        );
    }

    @Test
    void getUsers_shouldReturnOk_whenServiceReturnsPage() throws Exception {
        // given
        Pageable expected = PageRequest.of(0, 20);
        Page<UserSummaryResponse> page = Page.empty(expected);
        when(userService.getUsersBySearch(null, null, null, expected)).thenReturn(page);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/admin/users"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
        verify(userService).getUsersBySearch(null, null, null, expected);
    }

    @Test
    void getUsers_shouldPassFiltersToService_whenParamsProvided() throws Exception {
        // given
        Pageable expected = PageRequest.of(0, 20);
        Page<UserSummaryResponse> page = Page.empty(expected);
        when(userService.getUsersBySearch(
                AccountSearchType.NAME, "alice", AccountStatusType.ACTIVE, expected))
                .thenReturn(page);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/users")
                        .param("searchType", "NAME")
                        .param("search", "alice")
                        .param("status", "ACTIVE"));

        // then
        result.andExpect(status().isOk());
        verify(userService).getUsersBySearch(
                AccountSearchType.NAME, "alice", AccountStatusType.ACTIVE, expected);
    }

    @Test
    void getUsers_shouldPassPaginationToService_whenPageParamsProvided() throws Exception {
        // given
        Pageable expected = PageRequest.of(2, 5);
        Page<UserSummaryResponse> page = Page.empty(expected);
        when(userService.getUsersBySearch(null, null, null, expected)).thenReturn(page);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/admin/users")
                        .param("page", "2")
                        .param("size", "5"));

        // then
        result.andExpect(status().isOk());
        verify(userService).getUsersBySearch(null, null, null, expected);
    }

    @Test
    void updateUser_shouldReturnNoContent_whenAuthenticationAndBodyAreValid() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UpdateDTO request = new UpdateDTO("New Name");
        String json = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/admin/users/{employeeId}", userId)
                        .principal(authFor(userId.toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        // then
        result.andExpect(status().isNoContent());
        verify(userService).updateUser(eq(userId), any(UpdateDTO.class));
    }

    @Test
    void updateUser_shouldDelegateWithAuthenticationUserId_whenCalled() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UpdateDTO request = new UpdateDTO("Renamed");
        String json = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(
                patch("/api/v1/admin/users/{employeeId}", UUID.randomUUID())
                        .principal(authFor(userId.toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        // then
        verify(userService).updateUser(eq(userId), any(UpdateDTO.class));
    }

    @Test
    void updateUser_shouldThrow_whenAuthenticationNameIsNotUuid() {
        // given
        UUID employeeId = UUID.randomUUID();

        // when / then
        assertThatThrownBy(() -> mockMvc.perform(
                patch("/api/v1/admin/users/{employeeId}", employeeId)
                        .principal(authFor("not-a-uuid"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"X\"}")))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userService);
    }

    @Test
    void updateUser_shouldReject_whenBodyIsMalformed() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/v1/admin/users/{employeeId}", userId)
                        .principal(authFor(userId.toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"));

        // then
        result.andExpect(status().isBadRequest());
        verifyNoInteractions(userService);
    }
}