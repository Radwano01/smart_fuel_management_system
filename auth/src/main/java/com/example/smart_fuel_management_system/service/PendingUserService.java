package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.entity.PendingUser;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.UUID;

public interface PendingUserService {
    void save(PendingUser user);
    PendingUser findByEmail(String email);
    PendingUser findByPhone(String phone);
    boolean existsEmail(String email);
    boolean existsByPhone(String phone);
    void validateEmail(String email);
    void validatePhone(String phone);
}