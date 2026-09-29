package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.AuthUserResponse;
import com.example.smart_fuel_management_system.enums.AccountStatusType;
import com.example.smart_fuel_management_system.enums.RoleType;
import com.example.smart_fuel_management_system.repository.AuthRepository;
import com.example.smart_fuel_management_system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final AuthRepository authRepository;

    @Override
    public List<AuthUserResponse> getUsersByIds(List<UUID> userIds) {

        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        return authRepository
                .findAllByIdInAndRole(
                        userIds,
                        RoleType.USER
                )
                .stream()
                .map(auth -> new AuthUserResponse(
                        auth.getId(),
                        auth.getPhoneNumber(),
                        auth.getAccountStatusType(),
                        auth.getCreatedAt()
                ))
                .toList();
    }

    @Override
    public List<AuthUserResponse> searchByPhone(
            String search,
            Pageable pageable) {

        return authRepository
                .searchByPhoneNumberAndRole(
                        search,
                        RoleType.USER,
                        null,
                        pageable
                )
                .getContent()
                .stream()
                .map(auth -> new AuthUserResponse(
                        auth.getId(),
                        auth.getPhoneNumber(),
                        auth.getAccountStatusType(),
                        auth.getCreatedAt()
                ))
                .toList();
    }

    @Override
    public List<AuthUserResponse> getUsersByStatus(
            AccountStatusType status,
            Pageable pageable) {

        return authRepository
                .findByAccountStatusTypeAndRole(
                        status,
                        RoleType.USER,
                        pageable
                )
                .getContent()
                .stream()
                .map(auth -> new AuthUserResponse(
                        auth.getId(),
                        auth.getPhoneNumber(),
                        auth.getAccountStatusType(),
                        auth.getCreatedAt()
                ))
                .toList();
    }
}