package com.example.smart_fuel_management_system.service.impl.auth.otp;

import com.example.smart_fuel_management_system.dto.NotificationEvent;
import com.example.smart_fuel_management_system.entity.PendingIdentifierChange;
import com.example.smart_fuel_management_system.enums.OtpType;
import com.example.smart_fuel_management_system.exceptions.InvalidOtpException;
import com.example.smart_fuel_management_system.service.NotificationPublisher;
import com.example.smart_fuel_management_system.service.OtpStorageService;
import com.example.smart_fuel_management_system.service.PendingIdentifierChangeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentifierChangeOtpServiceImplTest {

    @Mock
    private PendingIdentifierChangeService pendingIdentifierChangeService;

    @Mock
    private OtpStorageService otpStorageService;

    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private IdentifierChangeOtpServiceImpl identifierChangeOtpService;


    @Test
    void sendOtp_shouldSaveEmailOtpAndSendEmailNotification() {

        // given
        PendingIdentifierChange change =
                PendingIdentifierChange.builder()
                        .id(UUID.randomUUID())
                        .type(OtpType.EMAIL)
                        .newIdentifier("new@email.com")
                        .fullName("Radwan Rahmoun")
                        .build();

        // when
        identifierChangeOtpService.sendOtp(change);

        // then
        ArgumentCaptor<String> otpCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(otpStorageService)
                .saveEmailOtp(
                        eq("new@email.com"),
                        otpCaptor.capture()
                );

        String otp = otpCaptor.getValue();

        assertThat(otp)
                .matches("\\d{6}");

        ArgumentCaptor<NotificationEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        NotificationEvent.class
                );

        verify(notificationPublisher)
                .sendEmailOtp(eventCaptor.capture());

        NotificationEvent event =
                eventCaptor.getValue();

        assertThat(event.destination())
                .isEqualTo("new@email.com");

        assertThat(event.fullName())
                .isEqualTo("Radwan Rahmoun");

        assertThat(event.otp())
                .isEqualTo(otp);

        assertThat(event.token())
                .isEmpty();
    }


    @Test
    void sendOtp_shouldSavePhoneOtpAndSendSmsNotification() {

        // given
        PendingIdentifierChange change =
                PendingIdentifierChange.builder()
                        .id(UUID.randomUUID())
                        .type(OtpType.PHONE)
                        .newIdentifier("905551234567")
                        .fullName("Radwan Rahmoun")
                        .build();

        // when
        identifierChangeOtpService.sendOtp(change);

        // then
        verify(otpStorageService)
                .savePhoneOtp(
                        "905551234567",
                        "000000"
                );

        ArgumentCaptor<NotificationEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        NotificationEvent.class
                );

        verify(notificationPublisher)
                .sendSmsOtp(eventCaptor.capture());

        NotificationEvent event =
                eventCaptor.getValue();

        assertThat(event.destination())
                .isEqualTo("905551234567");

        assertThat(event.fullName())
                .isEqualTo("Radwan Rahmoun");

        assertThat(event.otp())
                .isEqualTo("000000");

        assertThat(event.token())
                .isEmpty();
    }


    @Test
    void verifyOtp_shouldDeleteOtpAndPendingChange_whenEmailOtpIsValid() {

        // given
        UUID changeId = UUID.randomUUID();

        PendingIdentifierChange change =
                PendingIdentifierChange.builder()
                        .id(changeId)
                        .type(OtpType.EMAIL)
                        .newIdentifier("new@email.com")
                        .build();

        when(pendingIdentifierChangeService
                .findById(changeId))
                .thenReturn(change);

        when(otpStorageService
                .getEmailOtp("new@email.com"))
                .thenReturn("123456");

        // when
        identifierChangeOtpService.verifyOtp(
                changeId,
                "123456"
        );

        // then
        verify(otpStorageService)
                .deleteEmailOtp("new@email.com");

        verify(pendingIdentifierChangeService)
                .delete(changeId);

        verify(otpStorageService, never())
                .deletePhoneOtp(anyString());
    }


    @Test
    void verifyOtp_shouldDeleteOtpAndPendingChange_whenPhoneOtpIsValid() {

        // given
        UUID changeId = UUID.randomUUID();

        PendingIdentifierChange change =
                PendingIdentifierChange.builder()
                        .id(changeId)
                        .type(OtpType.PHONE)
                        .newIdentifier("905551234567")
                        .build();

        when(pendingIdentifierChangeService
                .findById(changeId))
                .thenReturn(change);

        // when
        identifierChangeOtpService.verifyOtp(
                changeId,
                "000000"
        );

        // then
        verify(otpStorageService)
                .deletePhoneOtp("905551234567");

        verify(pendingIdentifierChangeService)
                .delete(changeId);

        verify(otpStorageService, never())
                .deleteEmailOtp(anyString());
    }


    @Test
    void verifyOtp_shouldThrowException_whenEmailOtpIsInvalid() {

        // given
        UUID changeId = UUID.randomUUID();

        PendingIdentifierChange change =
                PendingIdentifierChange.builder()
                        .id(changeId)
                        .type(OtpType.EMAIL)
                        .newIdentifier("new@email.com")
                        .build();

        when(pendingIdentifierChangeService
                .findById(changeId))
                .thenReturn(change);

        when(otpStorageService
                .getEmailOtp("new@email.com"))
                .thenReturn("123456");

        // when & then
        assertThatThrownBy(() ->
                identifierChangeOtpService.verifyOtp(
                        changeId,
                        "999999"
                )
        )
                .isInstanceOf(InvalidOtpException.class)
                .hasMessage("Invalid Email OTP");

        verify(otpStorageService, never())
                .deleteEmailOtp(anyString());

        verify(pendingIdentifierChangeService, never())
                .delete(any());
    }


    @Test
    void verifyOtp_shouldThrowException_whenPhoneOtpIsInvalid() {

        // given
        UUID changeId = UUID.randomUUID();

        PendingIdentifierChange change =
                PendingIdentifierChange.builder()
                        .id(changeId)
                        .type(OtpType.PHONE)
                        .newIdentifier("905551234567")
                        .build();

        when(pendingIdentifierChangeService
                .findById(changeId))
                .thenReturn(change);

        // when & then
        assertThatThrownBy(() ->
                identifierChangeOtpService.verifyOtp(
                        changeId,
                        "999999"
                )
        )
                .isInstanceOf(InvalidOtpException.class)
                .hasMessage("Invalid Phone OTP");

        verify(otpStorageService, never())
                .deletePhoneOtp(anyString());

        verify(pendingIdentifierChangeService, never())
                .delete(any());
    }


    @Test
    void resendOtp_shouldFindChangeAndSendOtp() {

        // given
        UUID changeId = UUID.randomUUID();

        PendingIdentifierChange change =
                PendingIdentifierChange.builder()
                        .id(changeId)
                        .type(OtpType.EMAIL)
                        .newIdentifier("new@email.com")
                        .fullName("Radwan Rahmoun")
                        .build();

        when(pendingIdentifierChangeService
                .findById(changeId))
                .thenReturn(change);

        // when
        identifierChangeOtpService.resendOtp(changeId);

        // then
        verify(pendingIdentifierChangeService)
                .findById(changeId);

        verify(otpStorageService)
                .saveEmailOtp(
                        eq("new@email.com"),
                        anyString()
                );

        verify(notificationPublisher)
                .sendEmailOtp(any(NotificationEvent.class));
    }
}
