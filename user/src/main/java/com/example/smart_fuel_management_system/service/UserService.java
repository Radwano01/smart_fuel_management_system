package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.*;
import com.example.smart_fuel_management_system.enums.AccountSearchType;
import com.example.smart_fuel_management_system.enums.AccountStatusType;
import com.example.smart_fuel_management_system.enums.RoleType;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {
    void create(String payload) throws JsonProcessingException;
    UserDTO getUser(UUID userId);
    void updateUser(UUID userId, UpdateDTO updateDTO);
    UserResponse getUserResponse(UUID id);
    UserResponseToPaymentService getUserForPayment(UUID userId);
    UserDashboardSummaryResponse getDashboardSummary();
    void updateEmail(UUID userId, String email);
    List<UserResponse> getUsersByIds(List<UUID> ids);
    Page<UserSummaryResponse> getUsersBySearch(AccountSearchType searchType,
                                        String search,
                                        AccountStatusType status,
                                        Pageable pageable);
    List<UserResponse> getUsersByFullName(String fullName);
}
