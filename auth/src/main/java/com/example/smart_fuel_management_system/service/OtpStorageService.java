package com.example.smart_fuel_management_system.service;


public interface OtpStorageService {

    void saveEmailOtp(String email, String otp);

    String getEmailOtp(String email);

    void deleteEmailOtp(String email);

    void savePhoneOtp(String phone, String otp);

    String getPhoneOtp(String phone);

    void deletePhoneOtp(String phone);
}
