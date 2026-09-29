package com.example.smart_fuel_management_system.service.impl.auth.otp;

import com.example.smart_fuel_management_system.dto.NotificationEvent;
import com.example.smart_fuel_management_system.dto.ResendOtpRequest;
import com.example.smart_fuel_management_system.dto.VerifyOtpRequest;
import com.example.smart_fuel_management_system.entity.PendingUser;
import com.example.smart_fuel_management_system.enums.OtpType;
import com.example.smart_fuel_management_system.exceptions.InvalidOtpException;
import com.example.smart_fuel_management_system.service.NotificationPublisher;
import com.example.smart_fuel_management_system.service.RegistrationOtpService;
import com.example.smart_fuel_management_system.service.PendingUserService;
import com.example.smart_fuel_management_system.service.OtpStorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class RegistrationOtpServiceImpl implements RegistrationOtpService {

    private final PendingUserService pendingUserService;
    private final OtpStorageService otpStorageService;
    private final UserActivationService userActivationService;
    private final NotificationPublisher notificationPublisher;

    private final SecureRandom random = new SecureRandom();

    @Override
    public void sendRegistrationOtp(PendingUser user) {

        String emailOtp = generate();
        String phoneOtp = generate();

        otpStorageService.saveEmailOtp(user.getEmail(), emailOtp);
        otpStorageService.savePhoneOtp(user.getPhoneNumber(), phoneOtp);

        notificationPublisher.sendEmailOtp(
                new NotificationEvent(
                        user.getEmail(),
                        user.getFullName(),
                        emailOtp, ""
                )
        );

        notificationPublisher.sendSmsOtp(
                new NotificationEvent(
                        user.getPhoneNumber(),
                        user.getFullName(),
                        phoneOtp, ""
                )
        );
    }

    @Override
    public void verifyOTP(VerifyOtpRequest request) throws JsonProcessingException {

        if (request.type() == OtpType.EMAIL) {
            verifyEmail(request.identifier(), request.otp());
            return;
        }

        verifyPhone(request.identifier(), request.otp());
    }

    @Override
    public void resendOTP(ResendOtpRequest request) {

        if (request.type() == OtpType.EMAIL) {
            changeEmailOtp(request.identifier());
            return;
        }

        changePhoneOtp(request.identifier());
    }

    private void changeEmailOtp(String email) {

        PendingUser user = pendingUserService.findByEmail(email);

        String emailOtp = generate();

        otpStorageService.saveEmailOtp(user.getEmail(), emailOtp);

        notificationPublisher.sendEmailOtp(
                new NotificationEvent(
                        user.getEmail(),
                        user.getFullName(),
                        emailOtp,
                        ""
                )
        );

    }

    //recording to the paid software like twilio phone otp will be otp to simplify the testing process
    private void changePhoneOtp(String phone) {

        PendingUser user = pendingUserService.findByPhone(phone);

        String phoneOtp = "000000";

        otpStorageService.savePhoneOtp(user.getPhoneNumber(), phoneOtp);

        notificationPublisher.sendSmsOtp(
                new NotificationEvent(
                        user.getPhoneNumber(),
                        user.getFullName(),
                        phoneOtp,
                        ""
                )
        );
    }

    private void verifyEmail(String email, String otp) throws JsonProcessingException {
        PendingUser user = pendingUserService.findByEmail(email);

        if (user.isVerifiedEmail()) return;

        if (!otpStorageService.getEmailOtp(user.getEmail()).equals(otp)) {
            throw new InvalidOtpException("Invalid Email OTP");
        }

        user.setVerifiedEmail(true);
        pendingUserService.save(user);

        otpStorageService.deleteEmailOtp(user.getEmail());

        userActivationService.activateUser(user);
    }

    //NOT: RECORDING TO THE PAID ISSUE SERVICE(TWILIO) THE OTP WILL BE 000000 ALL THE TIME
    private void verifyPhone(String phone, String otp) throws JsonProcessingException {

        PendingUser user = pendingUserService.findByPhone(phone);

        if (user.isVerifiedPhoneNumber()) return;

        if (!"000000".equals(otp)) {
            throw new InvalidOtpException("Invalid Phone OTP");
        }

        user.setVerifiedPhoneNumber(true);
        pendingUserService.save(user);

        otpStorageService.deletePhoneOtp(user.getPhoneNumber());

        userActivationService.activateUser(user);
    }

    private String generate() {
        return String.valueOf(100000 + random.nextInt(900000));
    }
}