package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.UpdateIdentifierRequest;

import java.util.UUID;

public interface IdentifierChangeService {
    UUID change(UUID authId, UpdateIdentifierRequest request);
    void verifyOtp(UUID changeId, String otp);
    void resendOtp(UUID changeId);
}