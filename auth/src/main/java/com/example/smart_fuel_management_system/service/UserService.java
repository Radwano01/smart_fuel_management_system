package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.AuthUserResponse;
import com.example.smart_fuel_management_system.enums.AccountStatusType;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<AuthUserResponse> getUsersByIds(List<UUID> userIds);

    List<AuthUserResponse> searchByPhone(
            String search,
            Pageable pageable);
    List<AuthUserResponse> getUsersByStatus(AccountStatusType status, Pageable pageable);
}
