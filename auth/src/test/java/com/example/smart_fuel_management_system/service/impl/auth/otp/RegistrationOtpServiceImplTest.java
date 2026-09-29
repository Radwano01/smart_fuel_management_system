package com.example.smart_fuel_management_system.service.impl.auth.otp;

import com.example.smart_fuel_management_system.dto.NotificationEvent;
import com.example.smart_fuel_management_system.dto.ResendOtpRequest;
import com.example.smart_fuel_management_system.dto.VerifyOtpRequest;
import com.example.smart_fuel_management_system.entity.PendingUser;
import com.example.smart_fuel_management_system.enums.OtpType;
import com.example.smart_fuel_management_system.exceptions.InvalidOtpException;
import com.example.smart_fuel_management_system.service.NotificationPublisher;
import com.example.smart_fuel_management_system.service.OtpStorageService;
import com.example.smart_fuel_management_system.service.PendingUserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationOtpServiceImplTest {

    @Mock
    private PendingUserService pendingUserService;

    @Mock
    private OtpStorageService otpStorageService;

    @Mock
    private UserActivationService userActivationService;

    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private RegistrationOtpServiceImpl registrationOtpService;


    @Test
    void sendRegistrationOtp_shouldSaveAndSendEmailAndPhoneOtp() {

        // given
        PendingUser user = new PendingUser();
        user.setEmail("user@gmail.com");
        user.setPhoneNumber("905551234567");
        user.setFullName("Radwan Rahmoun");

        // when
        registrationOtpService.sendRegistrationOtp(user);

        // then
        verify(otpStorageService)
                .saveEmailOtp(
                        eq("user@gmail.com"),
                        argThat(otp -> otp.matches("\\d{6}"))
                );

        verify(otpStorageService)
                .savePhoneOtp(
                        eq("905551234567"),
                        argThat(otp -> otp.matches("\\d{6}"))
                );

        verify(notificationPublisher)
                .sendEmailOtp(any(NotificationEvent.class));

        verify(notificationPublisher)
                .sendSmsOtp(any(NotificationEvent.class));
    }


    @Test
    void verifyOTP_shouldVerifyEmailAndActivateUser_whenOtpIsValid()
            throws JsonProcessingException {

        // given
        VerifyOtpRequest request =
                new VerifyOtpRequest(
                        OtpType.EMAIL,
                        "user@gmail.com",
                        "123456"
                );

        PendingUser user = new PendingUser();
        user.setEmail("user@gmail.com");
        user.setVerifiedEmail(false);

        when(pendingUserService
                .findByEmail("user@gmail.com"))
                .thenReturn(user);

        when(otpStorageService
                .getEmailOtp("user@gmail.com"))
                .thenReturn("123456");

        // when
        registrationOtpService.verifyOTP(request);

        // then
        assertThat(user.isVerifiedEmail())
                .isTrue();

        verify(pendingUserService)
                .save(user);

        verify(otpStorageService)
                .deleteEmailOtp("user@gmail.com");

        verify(userActivationService)
                .activateUser(user);
    }


    @Test
    void verifyOTP_shouldVerifyPhoneAndActivateUser_whenOtpIsValid()
            throws JsonProcessingException {

        // given
        VerifyOtpRequest request =
                new VerifyOtpRequest(
                        OtpType.PHONE,
                        "905551234567",
                        "000000"
                );

        PendingUser user = new PendingUser();
        user.setPhoneNumber("905551234567");
        user.setVerifiedPhoneNumber(false);

        when(pendingUserService
                .findByPhone("905551234567"))
                .thenReturn(user);

        // when
        registrationOtpService.verifyOTP(request);

        // then
        assertThat(user.isVerifiedPhoneNumber())
                .isTrue();

        verify(pendingUserService)
                .save(user);

        verify(otpStorageService)
                .deletePhoneOtp("905551234567");

        verify(userActivationService)
                .activateUser(user);
    }


    @Test
    void verifyOTP_shouldThrowException_whenEmailOtpIsInvalid()
            throws JsonProcessingException {

        // given
        VerifyOtpRequest request =
                new VerifyOtpRequest(
                        OtpType.EMAIL,
                        "user@gmail.com",
                        "999999"
                );

        PendingUser user = new PendingUser();
        user.setEmail("user@gmail.com");
        user.setVerifiedEmail(false);

        when(pendingUserService
                .findByEmail("user@gmail.com"))
                .thenReturn(user);

        when(otpStorageService
                .getEmailOtp("user@gmail.com"))
                .thenReturn("123456");

        // when & then
        assertThatThrownBy(() ->
                registrationOtpService.verifyOTP(request)
        )
                .isInstanceOf(InvalidOtpException.class)
                .hasMessage("Invalid Email OTP");

        verify(pendingUserService, never())
                .save(any());

        verify(userActivationService, never())
                .activateUser(any());

        verify(otpStorageService, never())
                .deleteEmailOtp(anyString());
    }


    @Test
    void verifyOTP_shouldThrowException_whenPhoneOtpIsInvalid()
            throws JsonProcessingException {

        // given
        VerifyOtpRequest request =
                new VerifyOtpRequest(
                        OtpType.PHONE,
                        "905551234567",
                        "999999"
                );

        PendingUser user = new PendingUser();
        user.setPhoneNumber("905551234567");
        user.setVerifiedPhoneNumber(false);

        when(pendingUserService
                .findByPhone("905551234567"))
                .thenReturn(user);

        // when & then
        assertThatThrownBy(() ->
                registrationOtpService.verifyOTP(request)
        )
                .isInstanceOf(InvalidOtpException.class)
                .hasMessage("Invalid Phone OTP");

        verify(pendingUserService, never())
                .save(any());

        verify(userActivationService, never())
                .activateUser(any());

        verify(otpStorageService, never())
                .deletePhoneOtp(anyString());
    }


    @Test
    void verifyOTP_shouldDoNothing_whenEmailIsAlreadyVerified()
            throws JsonProcessingException {

        // given
        VerifyOtpRequest request =
                new VerifyOtpRequest(
                        OtpType.EMAIL,
                        "user@gmail.com",
                        "123456"
                );

        PendingUser user = new PendingUser();
        user.setEmail("user@gmail.com");
        user.setVerifiedEmail(true);

        when(pendingUserService
                .findByEmail("user@gmail.com"))
                .thenReturn(user);

        // when
        registrationOtpService.verifyOTP(request);

        // then
        verify(otpStorageService, never())
                .getEmailOtp(anyString());

        verify(pendingUserService, never())
                .save(any());

        verify(userActivationService, never())
                .activateUser(any());
    }


    @Test
    void verifyOTP_shouldDoNothing_whenPhoneIsAlreadyVerified()
            throws JsonProcessingException {

        // given
        VerifyOtpRequest request =
                new VerifyOtpRequest(
                        OtpType.PHONE,
                        "905551234567",
                        "000000"
                );

        PendingUser user = new PendingUser();
        user.setPhoneNumber("905551234567");
        user.setVerifiedPhoneNumber(true);

        when(pendingUserService
                .findByPhone("905551234567"))
                .thenReturn(user);

        // when
        registrationOtpService.verifyOTP(request);

        // then
        verify(pendingUserService, never())
                .save(any());

        verify(otpStorageService, never())
                .deletePhoneOtp(anyString());

        verify(userActivationService, never())
                .activateUser(any());
    }


    @Test
    void resendOTP_shouldGenerateAndSendNewEmailOtp() {

        // given
        ResendOtpRequest request =
                new ResendOtpRequest(
                        "user@gmail.com",
                        OtpType.EMAIL
                );

        PendingUser user = new PendingUser();
        user.setEmail("user@gmail.com");
        user.setFullName("Radwan Rahmoun");

        when(pendingUserService
                .findByEmail("user@gmail.com"))
                .thenReturn(user);

        // when
        registrationOtpService.resendOTP(request);

        // then
        verify(otpStorageService)
                .saveEmailOtp(
                        eq("user@gmail.com"),
                        argThat(otp -> otp.matches("\\d{6}"))
                );

        verify(notificationPublisher)
                .sendEmailOtp(any(NotificationEvent.class));
    }


    @Test
    void resendOTP_shouldSaveAndSendFixedPhoneOtp() {

        // given
        ResendOtpRequest request =
                new ResendOtpRequest(
                        "905551234567",
                        OtpType.PHONE
                );

        PendingUser user = new PendingUser();
        user.setPhoneNumber("905551234567");
        user.setFullName("Radwan Rahmoun");

        when(pendingUserService
                .findByPhone("905551234567"))
                .thenReturn(user);

        // when
        registrationOtpService.resendOTP(request);

        // then
        verify(otpStorageService)
                .savePhoneOtp(
                        "905551234567",
                        "000000"
                );

        verify(notificationPublisher)
                .sendSmsOtp(any(NotificationEvent.class));
    }
}
