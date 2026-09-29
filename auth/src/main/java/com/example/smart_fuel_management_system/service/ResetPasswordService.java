package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.ChangePasswordRequest;
import com.example.smart_fuel_management_system.dto.ResetPasswordRequest;
import com.example.smart_fuel_management_system.dto.ResetPasswordTokenRequest;

public interface ResetPasswordService {
    void resetPassword(ResetPasswordRequest request, String token);

    void createAndNotifyPasswordResetToken(ResetPasswordTokenRequest request);
}
