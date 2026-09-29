package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.*;
import com.example.smart_fuel_management_system.service.IdentifierChangeService;
import com.example.smart_fuel_management_system.service.LoginService;
import com.example.smart_fuel_management_system.service.RegistrationOtpService;
import com.example.smart_fuel_management_system.service.ResetPasswordService;
import com.example.smart_fuel_management_system.service.impl.auth.RegistrationServiceImpl;
import com.example.smart_fuel_management_system.service.impl.resetPassword.ChangePasswordService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginService authService;
    private final ResetPasswordService resetPasswordService;
    private final RegistrationOtpService otpVerificationService;
    private final RegistrationServiceImpl registrationServiceImpl;
    private final ChangePasswordService changePasswordService;
    private final IdentifierChangeService identifierChangeService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request)
            throws JsonProcessingException {

        registrationServiceImpl.register(request);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Void> verifyOTP(@Valid @RequestBody VerifyOtpRequest request)
            throws JsonProcessingException {

        otpVerificationService.verifyOTP(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.userLogin(request)
        );
    }

    @PostMapping("/admin/login")
    public ResponseEntity<LoginResponse> adminLogin(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.adminLogin(request)
        );
    }

    @PostMapping("/station/login")
    public ResponseEntity<LoginResponse> stationLogin(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.stationLogin(request)
        );
    }

    @GetMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestParam("refreshToken")
                                                 @NotBlank
                                                 @Pattern(regexp = "^[^.]+\\.[^.]+\\.[^.]+$") String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ResetPasswordTokenRequest request){
        resetPasswordService.createAndNotifyPasswordResetToken(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request, @RequestParam("token") String token){
        resetPasswordService.resetPassword(request, token);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, Principal principal){
        UUID id = UUID.fromString(principal.getName());
        changePasswordService.changePassword(request, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend")
    public ResponseEntity<Void> resendOTP(@Valid @RequestBody ResendOtpRequest request){
        otpVerificationService.resendOTP(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/identifier-change")
    public ResponseEntity<Void> changeIdentifier(
            Principal principal,
            @Valid @RequestBody UpdateIdentifierRequest request) {

        UUID authId = UUID.fromString(principal.getName());

        identifierChangeService.change(
                authId,
                request
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/identifier-change/{changeId}/verify")
    public ResponseEntity<Void> verifyOtp(
            @PathVariable String changeId,
            @Valid @RequestBody VerifyIdentifierChangeOtpRequest request) {
        UUID id = UUID.fromString(changeId);
        identifierChangeService.verifyOtp(
                id,
                request.otp()
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/identifier-change/{changeId}/resend")
    public ResponseEntity<Void> resendOtp(
            @PathVariable String changeId) {
        UUID id = UUID.fromString(changeId);
        identifierChangeService.resendOtp(id);

        return ResponseEntity.noContent().build();
    }
}