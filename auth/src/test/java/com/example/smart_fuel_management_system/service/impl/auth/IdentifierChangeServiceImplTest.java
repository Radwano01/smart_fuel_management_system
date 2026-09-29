package com.example.smart_fuel_management_system.service.impl.auth;

import com.example.smart_fuel_management_system.dto.UpdateIdentifierRequest;
import com.example.smart_fuel_management_system.dto.UserResponse;
import com.example.smart_fuel_management_system.entity.Auth;
import com.example.smart_fuel_management_system.entity.PendingIdentifierChange;
import com.example.smart_fuel_management_system.enums.OtpType;
import com.example.smart_fuel_management_system.repository.AuthRepository;
import com.example.smart_fuel_management_system.service.PendingIdentifierChangeService;
import com.example.smart_fuel_management_system.service.impl.IdentifierChangeOtpService;
import com.example.smart_fuel_management_system.service.impl.auth.client.UserClient;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentifierChangeServiceImplTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private PendingIdentifierChangeService pendingIdentifierChangeService;

    @Mock
    private IdentifierChangeOtpService identifierChangeOtpService;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private IdentifierChangeServiceImpl identifierChangeService;


    @Test
    void change_shouldCreatePendingChangeAndSendOtp() {

        // given
        UUID authId = UUID.randomUUID();

        UpdateIdentifierRequest request =
                new UpdateIdentifierRequest(
                        OtpType.EMAIL,
                        "new@email.com"
                );

        Auth auth = new Auth();
        auth.setId(authId);

        var user = mock(UserResponse.class);

        when(pendingIdentifierChangeService
                .existsByAuthId(authId))
                .thenReturn(false);

        when(authRepository.findById(authId))
                .thenReturn(Optional.of(auth));

        when(userClient.getUserById(authId))
                .thenReturn(user);

        when(user.fullName())
                .thenReturn("Radwan Rahmoun");

        // when
        UUID result =
                identifierChangeService.change(
                        authId,
                        request
                );

        // then
        ArgumentCaptor<PendingIdentifierChange> captor =
                ArgumentCaptor.forClass(
                        PendingIdentifierChange.class
                );

        verify(pendingIdentifierChangeService)
                .save(captor.capture());

        PendingIdentifierChange savedChange =
                captor.getValue();

        assertThat(result)
                .isEqualTo(savedChange.getId());

        assertThat(savedChange.getAuthId())
                .isEqualTo(authId);

        assertThat(savedChange.getFullName())
                .isEqualTo("Radwan Rahmoun");

        assertThat(savedChange.getType())
                .isEqualTo(OtpType.EMAIL);

        assertThat(savedChange.getNewIdentifier())
                .isEqualTo("new@email.com");

        assertThat(savedChange.isVerified())
                .isFalse();

        verify(identifierChangeOtpService)
                .sendOtp(savedChange);
    }


    @Test
    void change_shouldThrowException_whenChangeAlreadyPending() {

        // given
        UUID authId = UUID.randomUUID();

        UpdateIdentifierRequest request =
                new UpdateIdentifierRequest(
                        OtpType.EMAIL,
                        "new@email.com"
                );

        when(pendingIdentifierChangeService
                .existsByAuthId(authId))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() ->
                identifierChangeService.change(
                        authId,
                        request
                )
        )
                .isInstanceOf(EntityExistsException.class)
                .hasMessage(
                        "There is already a pending identifier change"
                );

        verifyNoInteractions(authRepository);
        verifyNoInteractions(userClient);
        verifyNoInteractions(identifierChangeOtpService);
    }


    @Test
    void change_shouldThrowException_whenAuthNotFound() {

        // given
        UUID authId = UUID.randomUUID();

        UpdateIdentifierRequest request =
                new UpdateIdentifierRequest(
                        OtpType.EMAIL,
                        "new@email.com"
                );

        when(pendingIdentifierChangeService
                .existsByAuthId(authId))
                .thenReturn(false);

        when(authRepository.findById(authId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                identifierChangeService.change(
                        authId,
                        request
                )
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Employee/User not found");

        verifyNoInteractions(userClient);

        verify(identifierChangeOtpService, never())
                .sendOtp(any());
    }


    @Test
    void verifyOtp_shouldUpdateEmail_whenOtpIsValid() {

        // given
        UUID changeId = UUID.randomUUID();
        UUID authId = UUID.randomUUID();

        PendingIdentifierChange change =
                PendingIdentifierChange.builder()
                        .id(changeId)
                        .authId(authId)
                        .type(OtpType.EMAIL)
                        .newIdentifier("new@email.com")
                        .build();

        Auth auth = new Auth();
        auth.setId(authId);
        auth.setEmail("old@email.com");

        when(pendingIdentifierChangeService
                .findById(changeId))
                .thenReturn(change);

        when(authRepository.findById(authId))
                .thenReturn(Optional.of(auth));

        // when
        identifierChangeService.verifyOtp(
                changeId,
                "123456"
        );

        // then
        verify(identifierChangeOtpService)
                .verifyOtp(
                        changeId,
                        "123456"
                );

        verify(userClient)
                .updateEmail(
                        authId,
                        "new@email.com"
                );

        assertThat(auth.getEmail())
                .isEqualTo("new@email.com");
    }


    @Test
    void verifyOtp_shouldUpdatePhoneNumber_whenTypeIsNotEmail() {

        // given
        UUID changeId = UUID.randomUUID();
        UUID authId = UUID.randomUUID();

        PendingIdentifierChange change =
                PendingIdentifierChange.builder()
                        .id(changeId)
                        .authId(authId)
                        .type(OtpType.PHONE)
                        .newIdentifier("905551234567")
                        .build();

        Auth auth = new Auth();
        auth.setId(authId);
        auth.setPhoneNumber("905551111111");

        when(pendingIdentifierChangeService
                .findById(changeId))
                .thenReturn(change);

        when(authRepository.findById(authId))
                .thenReturn(Optional.of(auth));

        // when
        identifierChangeService.verifyOtp(
                changeId,
                "123456"
        );

        // then
        verify(identifierChangeOtpService)
                .verifyOtp(
                        changeId,
                        "123456"
                );

        verify(userClient)
                .updateEmail(
                        authId,
                        "905551234567"
                );

        assertThat(auth.getPhoneNumber())
                .isEqualTo("905551234567");
    }


    @Test
    void resendOtp_shouldDelegateToOtpService() {

        // given
        UUID changeId = UUID.randomUUID();

        // when
        identifierChangeService.resendOtp(changeId);

        // then
        verify(identifierChangeOtpService)
                .resendOtp(changeId);
    }
}
