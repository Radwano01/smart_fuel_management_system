package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.AuthUserResponse;
import com.example.smart_fuel_management_system.dto.StationEmployeeResponse;
import com.example.smart_fuel_management_system.enums.AccountStatusType;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import com.example.smart_fuel_management_system.security.CustomUserDetailsService;
import com.example.smart_fuel_management_system.security.JWTGenerator;
import com.example.smart_fuel_management_system.service.EmployeeService;
import com.example.smart_fuel_management_system.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthInternalController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthInternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JWTGenerator jwtGenerator;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private UserService userService;

    @Test
    void getEmployeeInfo_shouldReturnEmployeeInfo() throws Exception {

        // given
        UUID employeeId = UUID.randomUUID();

        StationEmployeeResponse response =
                StationEmployeeResponse.builder()
                        .id(employeeId)
                        .build();

        when(employeeService.getEmployeeInfo(employeeId))
                .thenReturn(response);

        // when & then
        mockMvc.perform(
                        get(
                                "/api/v1/internal/auth/stations/{employeeId}/employee",
                                employeeId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().json(
                                objectMapper.writeValueAsString(response)
                        )
                );

        verify(employeeService)
                .getEmployeeInfo(employeeId);
    }

    @Test
    void getUsersByIds_shouldReturnUsers() throws Exception {

        // given
        List<UUID> userIds =
                List.of(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        List<AuthUserResponse> response = List.of();

        when(userService.getUsersByIds(userIds))
                .thenReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/v1/internal/auth/by-ids")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(userIds)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().json(
                                objectMapper.writeValueAsString(response)
                        )
                );

        verify(userService)
                .getUsersByIds(userIds);
    }

    @Test
    void searchByPhone_shouldReturnUsers() throws Exception {

        // given
        String search = "5331234567";
        List<AuthUserResponse> response = List.of();

        when(userService.searchByPhone(
                eq(search),
                any()
        )).thenReturn(response);

        // when & then
        mockMvc.perform(
                        get("/api/v1/internal/auth/search/phone")
                                .param("search", search)
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().json(
                                objectMapper.writeValueAsString(response)
                        )
                );

        verify(userService)
                .searchByPhone(
                        eq(search),
                        any()
                );
    }

    @Test
    void getUsersByStatus_shouldReturnUsers() throws Exception {

        // given
        AccountStatusType statusType =
                AccountStatusType.ACTIVE;

        List<AuthUserResponse> response = List.of();

        when(userService.getUsersByStatus(
                eq(statusType),
                any()
        )).thenReturn(response);

        // when & then
        mockMvc.perform(
                        get("/api/v1/internal/auth/filter/status")
                                .param("status", statusType.name())
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().json(
                                objectMapper.writeValueAsString(response)
                        )
                );

        verify(userService)
                .getUsersByStatus(
                        eq(statusType),
                        any()
                );
    }
}