    package com.example.smart_fuel_management_system.service.impl.auth.otp;

    import com.example.smart_fuel_management_system.dto.NotificationEvent;
    import com.example.smart_fuel_management_system.entity.PendingIdentifierChange;
    import com.example.smart_fuel_management_system.enums.OtpType;
    import com.example.smart_fuel_management_system.exceptions.InvalidOtpException;
    import com.example.smart_fuel_management_system.service.NotificationPublisher;
    import com.example.smart_fuel_management_system.service.OtpStorageService;
    import com.example.smart_fuel_management_system.service.PendingIdentifierChangeService;
    import com.example.smart_fuel_management_system.service.impl.IdentifierChangeOtpService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.security.SecureRandom;
    import java.util.UUID;

    @Service
    @RequiredArgsConstructor
    public class IdentifierChangeOtpServiceImpl
            implements IdentifierChangeOtpService {

        private final PendingIdentifierChangeService pendingIdentifierChangeService;
        private final OtpStorageService otpStorageService;
        private final NotificationPublisher notificationPublisher;

        private final SecureRandom random = new SecureRandom();

        @Override
        public void sendOtp(PendingIdentifierChange change) {

            String otp = generate();


            if (change.getType() == OtpType.EMAIL) {

                otpStorageService.saveEmailOtp(
                        change.getNewIdentifier(),
                        otp
                );

                notificationPublisher.sendEmailOtp(
                        new NotificationEvent(
                                change.getNewIdentifier(),
                                change.getFullName(),
                                otp,
                                ""
                        )
                );

            } else {

                // Testing instead of Twilio
                otp = "000000";

                otpStorageService.savePhoneOtp(
                        change.getNewIdentifier(),
                        otp
                );

                notificationPublisher.sendSmsOtp(
                        new NotificationEvent(
                                change.getNewIdentifier(),
                                change.getFullName(),
                                otp,
                                ""
                        )
                );
            }
        }

        @Override
        @Transactional
        public void verifyOtp(UUID changeId, String otp) {

            PendingIdentifierChange change =
                    pendingIdentifierChangeService.findById(changeId);

            if (change.getType() == OtpType.EMAIL) {


                String storedOtp =
                        otpStorageService.getEmailOtp(
                                change.getNewIdentifier()
                        );

                if (storedOtp == null || !storedOtp.equals(otp)) {
                    throw new InvalidOtpException(
                            "Invalid Email OTP"
                    );
                }

                otpStorageService.deleteEmailOtp(
                        change.getNewIdentifier()
                );

            } else {

                if (!"000000".equals(otp)) {
                    throw new InvalidOtpException(
                            "Invalid Phone OTP"
                    );
                }

                otpStorageService.deletePhoneOtp(
                        change.getNewIdentifier()
                );
            }

            pendingIdentifierChangeService.delete(
                    change.getId()
            );
        }

        @Override
        public void resendOtp(UUID changeId) {

            PendingIdentifierChange change =
                    pendingIdentifierChangeService.findById(changeId);

            sendOtp(change);
        }

        private String generate() {
            return String.valueOf(
                    100000 + random.nextInt(900000)
            );
        }
    }