package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.LoginRequest;
import com.example.smart_fuel_management_system.dto.LoginResponse;
import com.example.smart_fuel_management_system.dto.RefreshTokenResponse;

public interface LoginService {
    LoginResponse userLogin(LoginRequest request);

    LoginResponse adminLogin(LoginRequest request);

    LoginResponse stationLogin(LoginRequest request);

    RefreshTokenResponse refreshToken(String token);
}
