 package com.example.smart_fuel_management_system.service.impl.auth;

import com.example.smart_fuel_management_system.dto.RegisterRequest;
import com.example.smart_fuel_management_system.dto.RegisterStationRequest;
import com.example.smart_fuel_management_system.entity.PendingUser;
import com.example.smart_fuel_management_system.enums.RoleType;
import com.example.smart_fuel_management_system.repository.AuthRepository;
import com.example.smart_fuel_management_system.service.PendingUserService;
import com.example.smart_fuel_management_system.service.RegistrationOtpService;
import com.example.smart_fuel_management_system.service.impl.auth.client.StationClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private RegistrationOtpService registrationOtpService;

    @Mock
    private PendingUserService pendingUserService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StationClient stationClient;

    @InjectMocks
    private RegistrationServiceImpl registrationService;


    @Test
    void register_shouldSavePendingUserAndSendOtp()
            throws JsonProcessingException {

        // given
        RegisterRequest request =
                new RegisterRequest(
                        "user@gmail.com",
                        "Radwan Rahmoun",
                        "905551234567",
                        "password"
                );

        when(authRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(authRepository.existsByPhoneNumber(request.phoneNumber()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.password()))
                .thenReturn("encoded-password");

        // when
        registrationService.register(request);

        // then
        ArgumentCaptor<PendingUser> captor =
                ArgumentCaptor.forClass(PendingUser.class);

        verify(pendingUserService)
                .save(captor.capture());

        PendingUser savedUser =
                captor.getValue();

        assertThat(savedUser.getEmail())
                .isEqualTo(request.email());

        assertThat(savedUser.getFullName())
                .isEqualTo(request.fullName());

        assertThat(savedUser.getPhoneNumber())
                .isEqualTo(request.phoneNumber());

        assertThat(savedUser.getPassword())
                .isEqualTo("encoded-password");

        assertThat(savedUser.getRole())
                .isEqualTo(RoleType.USER);

        assertThat(savedUser.isVerifiedEmail())
                .isFalse();

        assertThat(savedUser.isVerifiedPhoneNumber())
                .isFalse();

        verify(registrationOtpService)
                .sendRegistrationOtp(savedUser);
    }


    @Test
    void registerStationEmployee_shouldSavePendingUserAndSendOtp()
            throws JsonProcessingException {

        // given
        RegisterStationRequest request =
                new RegisterStationRequest(
                        "employee@gmail.com",
                        "Station Employee",
                        "905551234567",
                        "password"
                );

        when(authRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(authRepository.existsByPhoneNumber(request.phoneNumber()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.password()))
                .thenReturn("encoded-password");

        // when
        registrationService.registerStationEmployee(request);

        // then
        ArgumentCaptor<PendingUser> captor =
                ArgumentCaptor.forClass(PendingUser.class);

        verify(pendingUserService)
                .save(captor.capture());

        PendingUser savedUser =
                captor.getValue();

        assertThat(savedUser.getEmail())
                .isEqualTo(request.email());

        assertThat(savedUser.getFullName())
                .isEqualTo(request.fullName());

        assertThat(savedUser.getPhoneNumber())
                .isEqualTo(request.phoneNumber());

        assertThat(savedUser.getPassword())
                .isEqualTo("encoded-password");

        assertThat(savedUser.getRole())
                .isEqualTo(RoleType.STATION);

        assertThat(savedUser.isVerifiedEmail())
                .isFalse();

        assertThat(savedUser.isVerifiedPhoneNumber())
                .isFalse();

        verify(registrationOtpService)
                .sendRegistrationOtp(savedUser);
    }


    @Test
    void register_shouldThrowException_whenEmailAlreadyExists()
            throws JsonProcessingException {

        // given
        RegisterRequest request =
                new RegisterRequest(
                        "user@gmail.com",
                        "Radwan Rahmoun",
                        "905551234567",
                        "password"
                );

        when(authRepository.existsByEmail(request.email()))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() ->
                registrationService.register(request)
        )
                .isInstanceOf(EntityExistsException.class)
                .hasMessage("email already valid");

        verify(authRepository, never())
                .existsByPhoneNumber(anyString());

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(pendingUserService, never())
                .save(any());

        verify(registrationOtpService, never())
                .sendRegistrationOtp(any());
    }


    @Test
    void register_shouldThrowException_whenPhoneAlreadyExists()
            throws JsonProcessingException {

        // given
        RegisterRequest request =
                new RegisterRequest(
                        "user@gmail.com",
                        "Radwan Rahmoun",
                        "905551234567",
                        "password"
                );

        when(authRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(authRepository.existsByPhoneNumber(request.phoneNumber()))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() ->
                registrationService.register(request)
        )
                .isInstanceOf(EntityExistsException.class)
                .hasMessage("phone already valid");

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(pendingUserService, never())
                .save(any());

        verify(registrationOtpService, never())
                .sendRegistrationOtp(any());
    }
}
