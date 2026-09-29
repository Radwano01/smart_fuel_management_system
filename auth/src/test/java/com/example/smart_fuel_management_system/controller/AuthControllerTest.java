package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.*;
import com.example.smart_fuel_management_system.enums.LoginMethodType;
import com.example.smart_fuel_management_system.enums.OtpType;
import com.example.smart_fuel_management_system.service.IdentifierChangeService;
import com.example.smart_fuel_management_system.service.LoginService;
import com.example.smart_fuel_management_system.service.RegistrationOtpService;
import com.example.smart_fuel_management_system.service.ResetPasswordService;
import com.example.smart_fuel_management_system.service.impl.auth.RegistrationServiceImpl;
import com.example.smart_fuel_management_system.service.impl.resetPassword.ChangePasswordService;
import com.example.smart_fuel_management_system.security.CustomUserDetailsService;
import com.example.smart_fuel_management_system.security.JWTGenerator;
import com.example.smart_fuel_management_system.rateLimit.RedisRateLimiter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoginService authService;

    @MockBean
    private ResetPasswordService resetPasswordService;

    @MockBean
    private RegistrationOtpService otpVerificationService;

    @MockBean
    private RegistrationServiceImpl registrationServiceImpl;

    @MockBean
    private ChangePasswordService changePasswordService;

    @MockBean
    private IdentifierChangeService identifierChangeService;

    @MockBean
    private JWTGenerator jwtGenerator;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    @Test
    void register_shouldReturnCreated() throws Exception {

        // given
        RegisterRequest request =
                new RegisterRequest(
                        "user@gmail.com",
                        "password",
                        "Radwan Rahmoun",
                        "5331234567"
                );
        // when & then
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated());

        verify(registrationServiceImpl)
                .register(request);
    }

    @Test
    void verifyOTP_shouldReturnOk() throws Exception {

        // given
        VerifyOtpRequest request =
                new VerifyOtpRequest(
                        OtpType.EMAIL,
                        "user@gmail.com",
                        "123456"
                );

        // when & then
        mockMvc.perform(
                        post("/api/v1/auth/verify-otp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk());

        verify(otpVerificationService)
                .verifyOTP(request);
    }

    @Test
    void login_shouldReturnLoginResponse() throws Exception {

        // given
        LoginRequest request =
                new LoginRequest(
                        LoginMethodType.EMAIL,
                        "user@gmail.com",
                        "password"
                );

        LoginResponse response =
                new LoginResponse(
                        "access-token",
                        "refresh-token"
                );

        when(authService.userLogin(request))
                .thenReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().json(
                                objectMapper.writeValueAsString(response)
                        )
                );

        verify(authService)
                .userLogin(request);
    }

    @Test
    void adminLogin_shouldReturnLoginResponse() throws Exception {

        // given
        LoginRequest request =
                new LoginRequest(
                        LoginMethodType.EMAIL,
                        "admin@gmail.com",
                        "password"
                );

        LoginResponse response =
                new LoginResponse(
                        "access-token",
                        "refresh-token"
                );

        when(authService.adminLogin(request))
                .thenReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/v1/auth/admin/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().json(
                                objectMapper.writeValueAsString(response)
                        )
                );

        verify(authService)
                .adminLogin(request);
    }

    @Test
    void stationLogin_shouldReturnLoginResponse() throws Exception {

        // given
        LoginRequest request =
                new LoginRequest(
                        LoginMethodType.EMAIL,
                        "station@gmail.com",
                        "password"
                );

        LoginResponse response =
                new LoginResponse(
                        "access-token",
                        "refresh-token"
                );

        when(authService.stationLogin(request))
                .thenReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/v1/auth/station/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().json(
                                objectMapper.writeValueAsString(response)
                        )
                );

        verify(authService)
                .stationLogin(request);
    }

    @Test
    void refreshToken_shouldReturnAccessToken() throws Exception {

        // given
        RefreshTokenResponse response =
                new RefreshTokenResponse("new-access-token");

        when(authService.refreshToken("header.payload.signature"))
                .thenReturn(response);

        // when & then
        mockMvc.perform(
                        get("/api/v1/auth/refresh")
                                .param(
                                        "refreshToken",
                                        "header.payload.signature"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().json(
                                objectMapper.writeValueAsString(response)
                        )
                );

        verify(authService)
                .refreshToken("header.payload.signature");
    }

    @Test
    void forgotPassword_shouldReturnOk() throws Exception {

        // given
        ResetPasswordTokenRequest request =
                new ResetPasswordTokenRequest(
                        "user@gmail.com"
                );

        // when & then
        mockMvc.perform(
                        post("/api/v1/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk());

        verify(resetPasswordService)
                .createAndNotifyPasswordResetToken(request);
    }

    @Test
    void resetPassword_shouldReturnOk() throws Exception {

        // given
        ResetPasswordRequest request =
                new ResetPasswordRequest(
                        "newPassword"
                );

        // when & then
        mockMvc.perform(
                        patch("/api/v1/auth/reset-password")
                                .param("token", "reset-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk());

        verify(resetPasswordService)
                .resetPassword(
                        request,
                        "reset-token"
                );
    }

    @Test
    void changePassword_shouldReturnOk() throws Exception {

        // given
        UUID userId = UUID.randomUUID();

        ChangePasswordRequest request =
                new ChangePasswordRequest(
                        "oldPassword",
                        "newPassword"
                );

        // when & then
        mockMvc.perform(
                        patch("/api/v1/auth/change-password")
                                .principal(
                                        () -> userId.toString()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk());

        verify(changePasswordService)
                .changePassword(
                        request,
                        userId
                );
    }

    @Test
    void resendOTP_shouldReturnOk() throws Exception {

        // given
        ResendOtpRequest request =
                new ResendOtpRequest(
                        "user@gmail.com",
                        OtpType.EMAIL
                );

        // when & then
        mockMvc.perform(
                        post("/api/v1/auth/resend")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk());

        verify(otpVerificationService)
                .resendOTP(request);
    }

    @Test
    void changeIdentifier_shouldReturnNoContent() throws Exception {

        // given
        UUID authId = UUID.randomUUID();

        UpdateIdentifierRequest request =
                new UpdateIdentifierRequest(
                        OtpType.EMAIL,
                        "new@gmail.com"
                );

        // when & then
        mockMvc.perform(
                        post("/api/v1/auth/identifier-change")
                                .principal(
                                        authId::toString
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isNoContent());

        verify(identifierChangeService)
                .change(
                        eq(authId),
                        any(UpdateIdentifierRequest.class)
                );
    }

    @Test
    void verifyOtp_shouldReturnNoContent() throws Exception {

        // given
        UUID changeId = UUID.randomUUID();

        VerifyIdentifierChangeOtpRequest request =
                new VerifyIdentifierChangeOtpRequest(
                        "123456"
                );

        // when & then
        mockMvc.perform(
                        post(
                                "/api/v1/auth/identifier-change/{changeId}/verify",
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
    void resendIdentifierChangeOtp_shouldReturnNoContent() throws Exception {

        // given
        UUID changeId = UUID.randomUUID();

        // when & then
        mockMvc.perform(
                        post(
                                "/api/v1/auth/identifier-change/{changeId}/resend",
                                changeId
                        )
                )
                .andExpect(status().isNoContent());

        verify(identifierChangeService)
                .resendOtp(changeId);
    }
}