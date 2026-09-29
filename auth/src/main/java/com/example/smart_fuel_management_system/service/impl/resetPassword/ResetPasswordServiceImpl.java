package com.example.smart_fuel_management_system.service.impl.resetPassword;

import com.example.smart_fuel_management_system.dto.NotificationEvent;
import com.example.smart_fuel_management_system.dto.ResetPasswordRequest;
import com.example.smart_fuel_management_system.dto.ResetPasswordTokenRequest;
import com.example.smart_fuel_management_system.dto.UserResponse;
import com.example.smart_fuel_management_system.entity.Auth;
import com.example.smart_fuel_management_system.entity.PasswordResetToken;
import com.example.smart_fuel_management_system.repository.AuthRepository;
import com.example.smart_fuel_management_system.service.NotificationPublisher;
import com.example.smart_fuel_management_system.service.ResetPasswordService;
import com.example.smart_fuel_management_system.service.impl.auth.client.UserClient;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResetPasswordServiceImpl implements ResetPasswordService {

    private final PasswordResetTokenService tokenService;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserClient userClient;
    private final NotificationPublisher notificationPublisher;

    @Transactional
    @Override
    public void resetPassword(ResetPasswordRequest request, String token) {

        UUID userId = tokenService.validateToken(token);

        Auth user = authRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("user not found"));

        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    @Override
    public void createAndNotifyPasswordResetToken(ResetPasswordTokenRequest request) {

        UUID userId = authRepository.findIdByEmail(request.email());

        UserResponse user = userClient.getUserById(userId);

        PasswordResetToken resetToken = tokenService.createToken(user.id());

        notificationPublisher.sendResetPasswordToken(
                new NotificationEvent(
                        request.email(),
                        user.fullName(),
                        "",
                        resetToken.getToken()
                )
        );
    }
}