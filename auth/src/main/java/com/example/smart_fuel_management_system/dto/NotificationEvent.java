package com.example.smart_fuel_management_system.dto;


//token job: when the notification for reset password part
public record NotificationEvent(String destination,
                                String fullName,
                                String otp,
                                String token) {
}
