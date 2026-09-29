package com.example.smart_fuel_management_system.service.impl.auth;

import com.example.smart_fuel_management_system.dto.*;
import com.example.smart_fuel_management_system.entity.Auth;
import com.example.smart_fuel_management_system.enums.AccountSearchType;
import com.example.smart_fuel_management_system.enums.AccountStatusType;
import com.example.smart_fuel_management_system.enums.RoleType;
import com.example.smart_fuel_management_system.exceptions.BadRequestException;
import com.example.smart_fuel_management_system.repository.AuthRepository;
import com.example.smart_fuel_management_system.service.EmployeeService;
import com.example.smart_fuel_management_system.service.impl.auth.client.StationClient;
import com.example.smart_fuel_management_system.service.impl.auth.client.UserClient;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final PasswordEncoder passwordEncoder;
    private final AuthRepository authRepository;
    private final UserClient userClient;
    private final StationClient stationClient;

    @Transactional(readOnly = true)
    @Override
    public StationEmployeeResponse getEmployeeInfo(UUID employeeId) {

        Auth auth = findAuth(employeeId, RoleType.STATION);

        UserResponse user = userClient.getUserById(auth.getId());

        return StationEmployeeResponse.builder()
                .id(auth.getId())
                .fullName(user.fullName())
                .email(auth.getEmail())
                .phoneNumber(auth.getPhoneNumber())
                .statusType(auth.getAccountStatusType())
                .statusType(auth.getAccountStatusType())
                .createdAt(auth.getCreatedAt())
                .updatedAt(auth.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public EmployeeDetailsResponse getEmployeeDetails(UUID employeeId) {

        Auth auth = findAuth(employeeId, RoleType.STATION);

        UserResponse user = userClient.getUserById(auth.getId());

        StationEmployeeAuthResponse station =
                stationClient.getStationDetails(auth.getId())
                        .orElse(null);

        return EmployeeDetailsResponse.builder()
                .id(auth.getId())
                .fullName(user.fullName())
                .email(auth.getEmail())
                .phoneNumber(auth.getPhoneNumber())
                .statusType(auth.getAccountStatusType())
                .createdAt(auth.getCreatedAt())
                .updatedAt(auth.getUpdatedAt())
                .station(station)
                .build();
    }

    @Transactional
    @Override
    public void update(
            UUID employeeId,
            UpdateEmployeeRequest request) {

        Auth auth = findAuth(employeeId, RoleType.STATION);

        if (request.password() != null &&
                !request.password().isBlank()) {

            auth.setPassword(
                    passwordEncoder.encode(request.password())
            );
        }

        if (request.accountStatusType() != null) {
            auth.setAccountStatusType(
                    request.accountStatusType()
            );
        }
    }

    @Transactional
    @Override
    public void update(UUID employeeId, UpdateUserRequest request) {
        Auth auth = findAuth(employeeId, RoleType.USER);
        if (request.accountStatusType() != null) {
            auth.setAccountStatusType(
                    request.accountStatusType()
            );
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Page<EmployeeResponse> search(
            AccountSearchType searchType,
            String search,
            AccountStatusType status,
            Pageable pageable) {

        if (search == null || search.isBlank()) {
            return searchAll(status, pageable);
        }

        if (searchType == null) {
            throw new BadRequestException(
                    "Search type is required when search is provided"
            );
        }

        String normalizedSearch = search.trim();

        return switch (searchType) {

            case EMAIL ->
                    searchByEmail(
                            normalizedSearch,
                            status,
                            pageable
                    );

            case PHONE ->
                    searchByPhone(
                            normalizedSearch,
                            status,
                            pageable
                    );

            case NAME ->
                    searchByName(
                            normalizedSearch,
                            status,
                            pageable
                    );
        };
    }

    private Page<EmployeeResponse> searchAll(
            AccountStatusType status,
            Pageable pageable) {

        if (status == null) {
            return mapEmployeesWithUsers(
                    authRepository.findAllByRole(
                            RoleType.STATION,
                            pageable
                    )
            );
        }

        return mapEmployeesWithUsers(
                authRepository.findByAccountStatusTypeAndRole(
                        status,
                        RoleType.STATION,
                        pageable
                )
        );
    }

    private Page<EmployeeResponse> searchByEmail(
            String search,
            AccountStatusType status,
            Pageable pageable) {

        Page<Auth> employees =
                authRepository.searchByEmailAndRole(
                        search,
                        RoleType.STATION,
                        status,
                        pageable
                );

        return mapEmployeesWithUsers(employees);
    }

    private Page<EmployeeResponse> searchByPhone(
            String search,
            AccountStatusType status,
            Pageable pageable) {

        Page<Auth> employees =
                authRepository.searchByPhoneNumberAndRole(
                        search,
                        RoleType.STATION,
                        status,
                        pageable
                );

        return mapEmployeesWithUsers(employees);
    }

    private Page<EmployeeResponse> searchByName(
            String search,
            AccountStatusType status,
            Pageable pageable) {

        List<UserResponse> users =
                userClient.searchUsersByName(search);

        if (users.isEmpty()) {
            return Page.empty(pageable);
        }

        List<UUID> userIds = users.stream()
                .map(UserResponse::id)
                .toList();

        Page<Auth> employees =
                authRepository.searchByIdsAndRole(
                        userIds,
                        RoleType.STATION,
                        status,
                        pageable
                );

        return mapEmployeesWithUsers(employees);
    }

    private Page<EmployeeResponse> mapEmployeesWithUsers(
            Page<Auth> employees) {

        if (employees.isEmpty()) {
            return employees.map(employee ->
                    toEmployeeResponse(employee, null)
            );
        }

        List<UUID> employeeIds =
                employees.getContent()
                        .stream()
                        .map(Auth::getId)
                        .toList();

        List<UserResponse> users =
                userClient.getUsersByIds(employeeIds);

        Map<UUID, UserResponse> usersById =
                users.stream()
                        .collect(Collectors.toMap(
                                UserResponse::id,
                                Function.identity()
                        ));

        return employees.map(employee ->
                toEmployeeResponse(
                        employee,
                        usersById.get(employee.getId())
                )
        );
    }

    private EmployeeResponse toEmployeeResponse(
            Auth employee,
            UserResponse user) {

        return new EmployeeResponse(
                employee.getId(),
                user != null ? user.fullName() : null,
                employee.getEmail(),
                employee.getPhoneNumber(),
                employee.getAccountStatusType(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }

    private Auth findAuth(UUID employeeId, RoleType roleType) {
        return authRepository.findByIdAndRole(employeeId, roleType)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Station employee does not exist!"
                        )
                );
    }
}