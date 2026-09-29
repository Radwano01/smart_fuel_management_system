package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.ResendOtpRequest;
import com.example.smart_fuel_management_system.dto.VerifyOtpRequest;
import com.example.smart_fuel_management_system.entity.PendingUser;
import com.fasterxml.jackson.core.JsonProcessingException;


public interface RegistrationOtpService {
    void sendRegistrationOtp(PendingUser user) throws JsonProcessingException;
    void verifyOTP(VerifyOtpRequest request) throws JsonProcessingException;
    void resendOTP(ResendOtpRequest request);
}
