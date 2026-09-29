package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.entity.User;
import com.example.smart_fuel_management_system.dto.*;
import com.example.smart_fuel_management_system.enums.AccountSearchType;
import com.example.smart_fuel_management_system.enums.AccountStatusType;
import com.example.smart_fuel_management_system.enums.RoleType;
import com.example.smart_fuel_management_system.exceptions.BadRequestException;
import com.example.smart_fuel_management_system.repository.UserRepository;
import com.example.smart_fuel_management_system.service.UserService;
import com.example.smart_fuel_management_system.service.impl.client.AuthClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final AuthClient authClient;

    @Override
    public void create(String payload) throws JsonProcessingException {
        SaveUser createdUser = objectMapper.readValue(
                payload,
                SaveUser.class
        );

        User user = new User(
                createdUser.id(),
                createdUser.fullName(),
                createdUser.email(),
                createdUser.createdAt(),
                createdUser.updatedAt()
        );

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDTO getUser(UUID userId) {

        User user = findUserById(userId);

        return UserDTO.builder()
                .fullName(user.getFullName())
                .build();
    }

    @Override
    @Transactional
    public void updateUser(UUID userId, UpdateDTO updateDTO) {
        User user = findUserById(userId);

        if (updateDTO.fullName() != null && !updateDTO.fullName().equals(user.getFullName())) {
            user.setFullName(updateDTO.fullName());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public UserResponse getUserResponse(UUID id) {
        return userRepository.findFullNameById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public UserResponseToPaymentService getUserForPayment(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found")
                );

        return new UserResponseToPaymentService(
                user.getFullName(),
                user.getEmail()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public UserDashboardSummaryResponse getDashboardSummary() {
        return new UserDashboardSummaryResponse(userRepository.count());
    }

    @Transactional
    @Override
    public void updateEmail(UUID userId, String email) {
        User user = findUserById(userId);

        user.setEmail(email);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserResponse> getUsersByIds(List<UUID> userIds) {

        return userRepository.findAllById(userIds)
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getFullName()
                ))
                .toList();
    }

    //todo: update this function to return only the users role
    @Transactional(readOnly = true)
    @Override
    public Page<UserSummaryResponse> getUsersBySearch(AccountSearchType searchType,
                                               String search,
                                               AccountStatusType status,
                                               Pageable pageable) {

        if (search != null && search.isBlank()) {
            search = null;
        }

        if (searchType == null || (search == null && status == null)) {
            return getLatestUsers(pageable);
        }

        return switch (searchType) {

            case NAME ->
                    searchByName(search, pageable);

            case EMAIL ->
                    searchByEmail(search, pageable);

            case PHONE ->
                    searchByPhone(search, pageable);

            case ACCOUNT_STATUS_TYPE ->
                    filterByStatus(status, pageable);
            };
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserResponse> getUsersByFullName(String fullName) {
        return userRepository.findByFullNameContainingIgnoreCase(fullName);
    }

    private Page<UserSummaryResponse> getLatestUsers(Pageable pageable) {

        Page<User> users =
                userRepository.findAllByOrderByCreatedAtDesc(pageable);

        return mapUsersWithAuth(users);
    }

    private Page<UserSummaryResponse> searchByName(
            String search,
            Pageable pageable) {

        if (search == null || search.isBlank()) {
            throw new BadRequestException("Search value cannot be empty");
        }

        Page<User> users =
                userRepository.findByFullNameContainingIgnoreCase(
                        search,
                        pageable
                );

        return mapUsersWithAuth(users);
    }

    private Page<UserSummaryResponse> searchByEmail(
            String search,
            Pageable pageable) {

        if (search == null || search.isBlank()) {
            throw new BadRequestException("Search value cannot be empty");
        }

        Page<User> users =
                userRepository.findByEmailContainingIgnoreCase(
                        search,
                        pageable
                );

        return mapUsersWithAuth(users);
    }

    private Page<UserSummaryResponse> searchByPhone(
            String search,
            Pageable pageable) {

        if (search == null || search.isBlank()) {
            throw new BadRequestException("Search value cannot be empty");
        }

        List<AuthUserResponse> authUsers =
                authClient.searchByPhone(search, pageable);

        return mapAuthUsersWithUsers(authUsers, pageable);
    }

    private Page<UserSummaryResponse> filterByStatus(
            AccountStatusType status,
            Pageable pageable) {

        if (status == null) {
            throw new BadRequestException("Account status cannot be empty");
        }

        List<AuthUserResponse> authUsers =
                authClient.getUsersByStatus(status, pageable);

        return mapAuthUsersWithUsers(authUsers, pageable);
    }

    private Page<UserSummaryResponse> mapUsersWithAuth(
            Page<User> users
    ) {

        List<UUID> userIds = users.getContent()
                .stream()
                .map(User::getId)
                .toList();

        List<AuthUserResponse> authUsers =
                authClient.getUsers(userIds);

        Map<UUID, AuthUserResponse> authMap =
                authUsers.stream()
                        .collect(Collectors.toMap(
                                AuthUserResponse::id,
                                Function.identity()
                        ));

        List<UserSummaryResponse> responses =
                users.getContent()
                        .stream()

                        // Keep only IDs returned by Auth Service.
                        // Auth Service returns only RoleType.USER.
                        .filter(user ->
                                authMap.containsKey(user.getId())
                        )

                        .map(user -> {

                            AuthUserResponse auth =
                                    authMap.get(user.getId());

                            return new UserSummaryResponse(
                                    user.getId(),
                                    user.getFullName(),
                                    user.getEmail(),
                                    auth.phoneNumber(),
                                    auth.accountStatusType(),
                                    user.getCreatedAt()
                            );
                        })
                        .toList();

        return new PageImpl<>(
                responses,
                users.getPageable(),
                responses.size()
        );
    }

    private Page<UserSummaryResponse> mapAuthUsersWithUsers(
            List<AuthUserResponse> authUsers,
            Pageable pageable
    ) {

        List<UUID> userIds = authUsers.stream()
                .map(AuthUserResponse::id)
                .toList();

        List<User> users =
                userRepository.findAllById(userIds);

        Map<UUID, User> userMap =
                users.stream()
                        .collect(Collectors.toMap(
                                User::getId,
                                Function.identity()
                        ));

        List<UserSummaryResponse> responses =
                authUsers.stream()
                        .map(auth -> {

                            User user = userMap.get(auth.id());

                            return new UserSummaryResponse(
                                    auth.id(),
                                    user != null ? user.getFullName() : null,
                                    user != null ? user.getEmail() : null,
                                    auth.phoneNumber(),
                                    auth.accountStatusType(),
                                    user != null ? user.getCreatedAt() : null
                            );
                        })
                        .toList();

        return new PageImpl<>(
                responses,
                pageable,
                responses.size()
        );
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "User id is not found in our database"
                        ));
    }
}