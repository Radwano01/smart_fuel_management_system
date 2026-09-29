package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.AuthUserResponse;
import com.example.smart_fuel_management_system.entity.Auth;
import com.example.smart_fuel_management_system.enums.AccountStatusType;
import com.example.smart_fuel_management_system.enums.RoleType;
import com.example.smart_fuel_management_system.repository.AuthRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private AuthRepository authRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUsersByIds_shouldReturnEmptyList_whenIdsAreNullOrEmpty() {

        // given
        List<UUID> userIds = null;

        // when
        List<AuthUserResponse> result = userService.getUsersByIds(userIds);

        // then
        assertThat(result).isEmpty();
        verifyNoInteractions(authRepository);
    }

    @Test
    void getUsersByIds_shouldReturnUsers_whenIdsExist() {

        // given
        UUID userId = UUID.randomUUID();

        Auth auth = new Auth();
        auth.setId(userId);
        auth.setPhoneNumber("05331234567");
        auth.setAccountStatusType(AccountStatusType.ACTIVE);
        auth.setCreatedAt(LocalDateTime.now());

        List<UUID> userIds = List.of(userId);

        when(authRepository.findAllByIdInAndRole(userIds, RoleType.USER))
                .thenReturn(List.of(auth));

        // when
        List<AuthUserResponse> result = userService.getUsersByIds(userIds);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(userId);
        assertThat(result.get(0).phoneNumber()).isEqualTo("05331234567");
        assertThat(result.get(0).accountStatusType())
                .isEqualTo(AccountStatusType.ACTIVE);

        verify(authRepository).findAllByIdInAndRole(userIds, RoleType.USER);
    }

    @Test
    void searchByPhone_shouldReturnUsers_whenUsersAreFound() {

        // given
        String search = "0533";
        Pageable pageable = PageRequest.of(0, 10);

        Auth auth = new Auth();
        auth.setId(UUID.randomUUID());
        auth.setPhoneNumber("05331234567");
        auth.setAccountStatusType(AccountStatusType.ACTIVE);
        auth.setCreatedAt(LocalDateTime.now());

        when(authRepository.searchByPhoneNumberAndRole(
                search,
                RoleType.USER,
                null,
                pageable
        )).thenReturn(new PageImpl<>(List.of(auth)));

        // when
        List<AuthUserResponse> result =
                userService.searchByPhone(search, pageable);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).phoneNumber())
                .isEqualTo("05331234567");

        verify(authRepository).searchByPhoneNumberAndRole(
                search,
                RoleType.USER,
                null,
                pageable
        );
    }

    @Test
    void getUsersByStatus_shouldReturnUsers_whenUsersAreFound() {

        // given
        AccountStatusType status = AccountStatusType.ACTIVE;
        Pageable pageable = PageRequest.of(0, 10);

        Auth auth = new Auth();
        auth.setId(UUID.randomUUID());
        auth.setPhoneNumber("05331234567");
        auth.setAccountStatusType(status);
        auth.setCreatedAt(LocalDateTime.now());

        when(authRepository.findByAccountStatusTypeAndRole(
                status,
                RoleType.USER,
                pageable
        )).thenReturn(new PageImpl<>(List.of(auth)));

        // when
        List<AuthUserResponse> result =
                userService.getUsersByStatus(status, pageable);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).accountStatusType())
                .isEqualTo(status);

        verify(authRepository).findByAccountStatusTypeAndRole(
                status,
                RoleType.USER,
                pageable
        );
    }
}
