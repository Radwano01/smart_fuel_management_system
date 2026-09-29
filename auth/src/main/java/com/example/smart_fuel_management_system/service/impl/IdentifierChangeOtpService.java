package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.entity.PendingIdentifierChange;

import java.util.UUID;

public interface IdentifierChangeOtpService {
    void sendOtp(PendingIdentifierChange change);
    void verifyOtp(UUID changeId, String otp);
    void resendOtp(UUID changeId);
}