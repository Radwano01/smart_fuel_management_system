package com.example.smart_fuel_management_system.service.impl.auth;

import com.example.smart_fuel_management_system.dto.*;
import com.example.smart_fuel_management_system.entity.Auth;
import com.example.smart_fuel_management_system.enums.AccountSearchType;
import com.example.smart_fuel_management_system.enums.AccountStatusType;
import com.example.smart_fuel_management_system.enums.RoleType;
import com.example.smart_fuel_management_system.exceptions.BadRequestException;
import com.example.smart_fuel_management_system.repository.AuthRepository;
import com.example.smart_fuel_management_system.service.impl.auth.client.StationClient;
import com.example.smart_fuel_management_system.service.impl.auth.client.UserClient;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private StationClient stationClient;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Test
    void getEmployeeInfo_shouldReturnEmployeeInfo_whenEmployeeExists() {

        // given
        UUID employeeId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(2);
        LocalDateTime updatedAt = LocalDateTime.now();

        Auth auth = new Auth();
        auth.setId(employeeId);
        auth.setEmail("employee@gmail.com");
        auth.setPhoneNumber("05331234567");
        auth.setAccountStatusType(AccountStatusType.ACTIVE);
        auth.setCreatedAt(createdAt);
        auth.setUpdatedAt(updatedAt);

        UserResponse user = new UserResponse(
                employeeId,
                "Radwan Rahmoun"
        );

        when(authRepository.findByIdAndRole(
                employeeId,
                RoleType.STATION
        )).thenReturn(Optional.of(auth));

        when(userClient.getUserById(employeeId))
                .thenReturn(user);

        // when
        StationEmployeeResponse result =
                employeeService.getEmployeeInfo(employeeId);

        // then
        assertThat(result.id()).isEqualTo(employeeId);
        assertThat(result.fullName()).isEqualTo("Radwan Rahmoun");
        assertThat(result.email()).isEqualTo("employee@gmail.com");
        assertThat(result.phoneNumber()).isEqualTo("05331234567");
        assertThat(result.statusType())
                .isEqualTo(AccountStatusType.ACTIVE);
        assertThat(result.createdAt()).isEqualTo(createdAt);
        assertThat(result.updatedAt()).isEqualTo(updatedAt);

        verify(authRepository).findByIdAndRole(
                employeeId,
                RoleType.STATION
        );
        verify(userClient).getUserById(employeeId);
    }

    @Test
    void getEmployeeInfo_shouldThrowException_whenEmployeeDoesNotExist() {

        // given
        UUID employeeId = UUID.randomUUID();

        when(authRepository.findByIdAndRole(
                employeeId,
                RoleType.STATION
        )).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() ->
                employeeService.getEmployeeInfo(employeeId)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Station employee does not exist!");

        verifyNoInteractions(userClient);
    }

    @Test
    void getEmployeeDetails_shouldReturnEmployeeDetails_withStation() {

        // given
        UUID employeeId = UUID.randomUUID();

        Auth auth = new Auth();
        auth.setId(employeeId);
        auth.setEmail("employee@gmail.com");
        auth.setPhoneNumber("05331234567");
        auth.setAccountStatusType(AccountStatusType.ACTIVE);

        UserResponse user = new UserResponse(
                employeeId,
                "Radwan Rahmoun"
        );

        StationEmployeeAuthResponse station =
                mock(StationEmployeeAuthResponse.class);

        when(authRepository.findByIdAndRole(
                employeeId,
                RoleType.STATION
        )).thenReturn(Optional.of(auth));

        when(userClient.getUserById(employeeId))
                .thenReturn(user);

        when(stationClient.getStationDetails(employeeId))
                .thenReturn(Optional.of(station));

        // when
        EmployeeDetailsResponse result =
                employeeService.getEmployeeDetails(employeeId);

        // then
        assertThat(result.id())
                .isEqualTo(employeeId);
        assertThat(result.fullName())
                .isEqualTo("Radwan Rahmoun");
        assertThat(result.email())
                .isEqualTo("employee@gmail.com");
        assertThat(result.phoneNumber())
                .isEqualTo("05331234567");
        assertThat(result.statusType())
                .isEqualTo(AccountStatusType.ACTIVE);
        assertThat(result.station())
                .isEqualTo(station);
    }

    @Test
    void getEmployeeDetails_shouldReturnDetails_withoutStation_whenStationDoesNotExist() {

        // given
        UUID employeeId = UUID.randomUUID();

        Auth auth = new Auth();
        auth.setId(employeeId);
        auth.setEmail("employee@gmail.com");
        auth.setPhoneNumber("05331234567");
        auth.setAccountStatusType(AccountStatusType.ACTIVE);

        UserResponse user = new UserResponse(
                employeeId,
                "Radwan Rahmoun"
        );

        when(authRepository.findByIdAndRole(
                employeeId,
                RoleType.STATION
        )).thenReturn(Optional.of(auth));

        when(userClient.getUserById(employeeId))
                .thenReturn(user);

        when(stationClient.getStationDetails(employeeId))
                .thenReturn(Optional.empty());

        // when
        EmployeeDetailsResponse result =
                employeeService.getEmployeeDetails(employeeId);

        // then
        assertThat(result.station()).isNull();
    }

    @Test
    void update_shouldChangePasswordAndStatus_whenValuesAreProvided() {

        // given
        UUID employeeId = UUID.randomUUID();

        Auth auth = new Auth();
        auth.setId(employeeId);
        auth.setPassword("oldPassword");

        UpdateEmployeeRequest request =
                new UpdateEmployeeRequest(
                        "newPassword",
                        AccountStatusType.INACTIVE
                );

        when(authRepository.findByIdAndRole(
                employeeId,
                RoleType.STATION
        )).thenReturn(Optional.of(auth));

        when(passwordEncoder.encode("newPassword"))
                .thenReturn("encodedNewPassword");

        // when
        employeeService.update(employeeId, request);

        // then
        assertThat(auth.getPassword())
                .isEqualTo("encodedNewPassword");
        assertThat(auth.getAccountStatusType())
                .isEqualTo(AccountStatusType.INACTIVE);

        verify(passwordEncoder).encode("newPassword");
    }

    @Test
    void update_shouldOnlyChangeStatus_whenPasswordIsBlank() {

        // given
        UUID employeeId = UUID.randomUUID();

        Auth auth = new Auth();
        auth.setId(employeeId);
        auth.setPassword("oldPassword");

        UpdateEmployeeRequest request =
                new UpdateEmployeeRequest(
                        " ",
                        AccountStatusType.INACTIVE
                );

        when(authRepository.findByIdAndRole(
                employeeId,
                RoleType.STATION
        )).thenReturn(Optional.of(auth));

        // when
        employeeService.update(employeeId, request);

        // then
        assertThat(auth.getPassword())
                .isEqualTo("oldPassword");
        assertThat(auth.getAccountStatusType())
                .isEqualTo(AccountStatusType.INACTIVE);

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void update_shouldChangeUserStatus_whenStatusIsProvided() {

        // given
        UUID userId = UUID.randomUUID();

        Auth auth = new Auth();
        auth.setId(userId);
        auth.setAccountStatusType(AccountStatusType.ACTIVE);

        UpdateUserRequest request =
                new UpdateUserRequest(AccountStatusType.BLOCKED);

        when(authRepository.findByIdAndRole(
                userId,
                RoleType.USER
        )).thenReturn(Optional.of(auth));

        // when
        employeeService.update(userId, request);

        // then
        assertThat(auth.getAccountStatusType())
                .isEqualTo(AccountStatusType.BLOCKED);

        verify(authRepository).findByIdAndRole(
                userId,
                RoleType.USER
        );
    }

    @Test
    void search_shouldReturnAllEmployees_whenSearchIsBlank() {

        // given
        Pageable pageable = PageRequest.of(0, 10);

        Auth employee = new Auth();
        employee.setId(UUID.randomUUID());
        employee.setEmail("employee@gmail.com");
        employee.setPhoneNumber("05331234567");
        employee.setAccountStatusType(AccountStatusType.ACTIVE);

        when(authRepository.findAllByRole(
                RoleType.STATION,
                pageable
        )).thenReturn(new PageImpl<>(List.of(employee)));

        when(userClient.getUsersByIds(List.of(employee.getId())))
                .thenReturn(List.of(
                        new UserResponse(
                                employee.getId(),
                                "Radwan Rahmoun"
                        )
                ));

        // when
        Page<EmployeeResponse> result =
                employeeService.search(
                        null,
                        "",
                        null,
                        pageable
                );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).fullName())
                .isEqualTo("Radwan Rahmoun");

        verify(authRepository).findAllByRole(
                RoleType.STATION,
                pageable
        );
    }

    @Test
    void search_shouldUseStatusFilter_whenSearchIsBlankAndStatusIsProvided() {

        // given
        Pageable pageable = PageRequest.of(0, 10);

        when(authRepository.findByAccountStatusTypeAndRole(
                AccountStatusType.ACTIVE,
                RoleType.STATION,
                pageable
        )).thenReturn(Page.empty(pageable));

        // when
        Page<EmployeeResponse> result =
                employeeService.search(
                        null,
                        null,
                        AccountStatusType.ACTIVE,
                        pageable
                );

        // then
        assertThat(result).isEmpty();

        verify(authRepository).findByAccountStatusTypeAndRole(
                AccountStatusType.ACTIVE,
                RoleType.STATION,
                pageable
        );

        verifyNoInteractions(userClient);
    }

    @Test
    void search_shouldThrowException_whenSearchTypeIsMissing() {

        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when / then
        assertThatThrownBy(() ->
                employeeService.search(
                        null,
                        "radwan",
                        null,
                        pageable
                )
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Search type is required when search is provided");

        verifyNoInteractions(authRepository, userClient);
    }

    @Test
    void search_shouldSearchByEmail_whenSearchTypeIsEmail() {

        // given
        Pageable pageable = PageRequest.of(0, 10);

        Auth employee = new Auth();
        employee.setId(UUID.randomUUID());
        employee.setEmail("employee@gmail.com");

        when(authRepository.searchByEmailAndRole(
                "employee@gmail.com",
                RoleType.STATION,
                null,
                pageable
        )).thenReturn(new PageImpl<>(List.of(employee)));

        when(userClient.getUsersByIds(List.of(employee.getId())))
                .thenReturn(List.of(
                        new UserResponse(
                                employee.getId(),
                                "Radwan Rahmoun"
                        )
                ));

        // when
        Page<EmployeeResponse> result =
                employeeService.search(
                        AccountSearchType.EMAIL,
                        " employee@gmail.com ",
                        null,
                        pageable
                );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).fullName())
                .isEqualTo("Radwan Rahmoun");

        verify(authRepository).searchByEmailAndRole(
                "employee@gmail.com",
                RoleType.STATION,
                null,
                pageable
        );
    }

    @Test
    void search_shouldSearchByPhone_whenSearchTypeIsPhone() {

        // given
        Pageable pageable = PageRequest.of(0, 10);

        Auth employee = new Auth();
        employee.setId(UUID.randomUUID());
        employee.setPhoneNumber("05331234567");

        when(authRepository.searchByPhoneNumberAndRole(
                "0533",
                RoleType.STATION,
                null,
                pageable
        )).thenReturn(new PageImpl<>(List.of(employee)));

        when(userClient.getUsersByIds(List.of(employee.getId())))
                .thenReturn(List.of(
                        new UserResponse(
                                employee.getId(),
                                "Radwan Rahmoun"
                        )
                ));

        // when
        Page<EmployeeResponse> result =
                employeeService.search(
                        AccountSearchType.PHONE,
                        " 0533 ",
                        null,
                        pageable
                );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).phoneNumber())
                .isEqualTo("05331234567");

        verify(authRepository).searchByPhoneNumberAndRole(
                "0533",
                RoleType.STATION,
                null,
                pageable
        );
    }

    @Test
    void search_shouldSearchByName_whenSearchTypeIsName() {

        // given
        Pageable pageable = PageRequest.of(0, 10);

        UUID employeeId = UUID.randomUUID();

        Auth employee = new Auth();
        employee.setId(employeeId);
        employee.setEmail("employee@gmail.com");

        when(userClient.searchUsersByName("Radwan"))
                .thenReturn(List.of(
                        new UserResponse(employeeId, "Radwan Rahmoun")
                ));

        when(authRepository.searchByIdsAndRole(
                List.of(employeeId),
                RoleType.STATION,
                null,
                pageable
        )).thenReturn(new PageImpl<>(List.of(employee)));

        when(userClient.getUsersByIds(List.of(employeeId)))
                .thenReturn(List.of(
                        new UserResponse(employeeId, "Radwan Rahmoun")
                ));

        // when
        Page<EmployeeResponse> result =
                employeeService.search(
                        AccountSearchType.NAME,
                        " Radwan ",
                        null,
                        pageable
                );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).fullName())
                .isEqualTo("Radwan Rahmoun");

        verify(userClient).searchUsersByName("Radwan");
        verify(authRepository).searchByIdsAndRole(
                List.of(employeeId),
                RoleType.STATION,
                null,
                pageable
        );
    }

    @Test
    void search_shouldReturnEmptyPage_whenNameSearchFindsNoUsers() {

        // given
        Pageable pageable = PageRequest.of(0, 10);

        when(userClient.searchUsersByName("Radwan"))
                .thenReturn(List.of());

        // when
        Page<EmployeeResponse> result =
                employeeService.search(
                        AccountSearchType.NAME,
                        "Radwan",
                        null,
                        pageable
                );

        // then
        assertThat(result).isEmpty();

        verify(userClient).searchUsersByName("Radwan");
        verifyNoInteractions(authRepository);
    }

    @Test
    void search_shouldReturnEmptyPage_whenRepositoryFindsNoEmployees() {

        // given
        Pageable pageable = PageRequest.of(0, 10);

        when(authRepository.searchByEmailAndRole(
                "missing@gmail.com",
                RoleType.STATION,
                null,
                pageable
        )).thenReturn(Page.empty(pageable));

        // when
        Page<EmployeeResponse> result =
                employeeService.search(
                        AccountSearchType.EMAIL,
                        "missing@gmail.com",
                        null,
                        pageable
                );

        // then
        assertThat(result).isEmpty();

        verify(authRepository).searchByEmailAndRole(
                "missing@gmail.com",
                RoleType.STATION,
                null,
                pageable
        );

        verifyNoInteractions(userClient);
    }
}
