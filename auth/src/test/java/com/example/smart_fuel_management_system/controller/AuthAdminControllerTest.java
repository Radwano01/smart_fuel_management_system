package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.EmployeeDetailsResponse;
import com.example.smart_fuel_management_system.dto.EmployeeResponse;
import com.example.smart_fuel_management_system.dto.RegisterStationRequest;
import com.example.smart_fuel_management_system.dto.UpdateEmployeeRequest;
import com.example.smart_fuel_management_system.dto.UpdateIdentifierRequest;
import com.example.smart_fuel_management_system.dto.UpdateUserRequest;
import com.example.smart_fuel_management_system.dto.VerifyIdentifierChangeOtpRequest;
import com.example.smart_fuel_management_system.enums.AccountSearchType;
import com.example.smart_fuel_management_system.enums.AccountStatusType;
import com.example.smart_fuel_management_system.enums.OtpType;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import com.example.smart_fuel_management_system.security.CustomUserDetailsService;
import com.example.smart_fuel_management_system.security.JWTGenerator;
import com.example.smart_fuel_management_system.service.EmployeeService;
import com.example.smart_fuel_management_system.service.IdentifierChangeService;
import com.example.smart_fuel_management_system.service.RegisterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthAdminControllerTest {

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
    private RegisterService registerService;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private IdentifierChangeService identifierChangeService;

    @Test
    void register_shouldReturnCreated() throws Exception {

        // given
        RegisterStationRequest request =
                new RegisterStationRequest(
                        "employee@gmail.com",
                        "Radwan Rahmoun",
                        "5331234567",
                        "password"
                );

        // when & then
        mockMvc.perform(
                        post("/api/v1/admin/auth/station-accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated());

        verify(registerService)
                .registerStationEmployee(
                        any(RegisterStationRequest.class)
                );
    }

    @Test
    void getEmployeeDetails_shouldReturnEmployeeDetails()
            throws Exception {

        // given
        UUID employeeId = UUID.randomUUID();

        EmployeeDetailsResponse response =
                EmployeeDetailsResponse.builder()
                        .id(employeeId)
                        .build();

        when(employeeService.getEmployeeDetails(employeeId))
                .thenReturn(response);

        // when & then
        mockMvc.perform(
                        get(
                                "/api/v1/admin/auth/employees/{employeeId}/details",
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
                .getEmployeeDetails(employeeId);
    }

    @Test
    void updateStationAccount_shouldReturnNoContent()
            throws Exception {

        // given
        UUID employeeId = UUID.randomUUID();

        UpdateEmployeeRequest request =
                new UpdateEmployeeRequest(
                        "newPassword",
                        AccountStatusType.ACTIVE
                );

        // when & then
        mockMvc.perform(
                        patch(
                                "/api/v1/admin/auth/station-accounts/{employeeId}",
                                employeeId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isNoContent());

        verify(employeeService)
                .update(
                        eq(employeeId),
                        any(UpdateEmployeeRequest.class)
                );
    }

    @Test
    void updateUserAccount_shouldReturnNoContent()
            throws Exception {

        // given
        UUID userId = UUID.randomUUID();

        UpdateUserRequest request =
                new UpdateUserRequest(
                        AccountStatusType.ACTIVE
                );

        // when & then
        mockMvc.perform(
                        patch("/api/v1/admin/auth/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isNoContent());

        verify(employeeService)
                .update(
                        eq(userId),
                        any(UpdateUserRequest.class)
                );
    }

    @Test
    void changeIdentifier_shouldReturnChangeId()
            throws Exception {

        // given
        UUID authId = UUID.randomUUID();
        UUID changeId = UUID.randomUUID();

        UpdateIdentifierRequest request =
                new UpdateIdentifierRequest(
                        OtpType.EMAIL,
                        "new@gmail.com"
                );

        when(identifierChangeService.change(
                eq(authId),
                any(UpdateIdentifierRequest.class)
        )).thenReturn(changeId);

        // when & then
        mockMvc.perform(
                        post(
                                "/api/v1/admin/auth/{authId}/identifier-change",
                                authId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().string(
                                objectMapper.writeValueAsString(changeId)
                        )
                );

        verify(identifierChangeService)
                .change(
                        eq(authId),
                        any(UpdateIdentifierRequest.class)
                );
    }

    @Test
    void verifyOtp_shouldReturnNoContent()
            throws Exception {

        // given
        UUID changeId = UUID.randomUUID();

        VerifyIdentifierChangeOtpRequest request =
                new VerifyIdentifierChangeOtpRequest("123456");

        // when & then
        mockMvc.perform(
                        post(
                                "/api/v1/admin/auth/identifier-change/{changeId}/verify",
                                changeId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isNoContent());

        verify(identifierChangeService)
                .verifyOtp(
                        eq(changeId),
                        eq("123456")
                );
    }

    @Test
    void resendOtp_shouldReturnNoContent()
            throws Exception {

        // given
        UUID changeId = UUID.randomUUID();

        // when & then
        mockMvc.perform(
                        post(
                                "/api/v1/admin/auth/identifier-change/{changeId}/resend",
                                changeId
                        )
                )
                .andExpect(status().isNoContent());

        verify(identifierChangeService)
                .resendOtp(changeId);
    }

    @Test
    void getEmployees_shouldReturnEmployees()
            throws Exception {

        // given
        UUID employeeId = UUID.randomUUID();

        EmployeeResponse employee =
                new EmployeeResponse(
                        employeeId,
                        "Radwan Rahmoun",
                        "employee@gmail.com",
                        "05331234567",
                        AccountStatusType.ACTIVE,
                        null,
                        null
                );

        Page<EmployeeResponse> page =
                new PageImpl<>(
                        List.of(employee),
                        PageRequest.of(0, 10),
                        1
                );

        when(employeeService.search(
                eq(AccountSearchType.NAME),
                eq("Radwan"),
                eq(AccountStatusType.ACTIVE),
                any()
        )).thenReturn(page);

        // when & then
        mockMvc.perform(
                        get("/api/v1/admin/auth/station-accounts")
                                .param("searchType", "NAME")
                                .param("search", "Radwan")
                                .param("status", "ACTIVE")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().json(
                                objectMapper.writeValueAsString(page)
                        )
                );

        verify(employeeService)
                .search(
                        eq(AccountSearchType.NAME),
                        eq("Radwan"),
                        eq(AccountStatusType.ACTIVE),
                        any()
                );
    }
}
