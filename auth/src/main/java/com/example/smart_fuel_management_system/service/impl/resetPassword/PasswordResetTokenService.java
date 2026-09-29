package com.example.smart_fuel_management_system.service.impl.resetPassword;

import com.example.smart_fuel_management_system.entity.PasswordResetToken;
import com.example.smart_fuel_management_system.exceptions.BadRequestException;
import com.example.smart_fuel_management_system.repository.PasswordResetTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {

    private final PasswordResetTokenRepository repository;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetToken createToken(UUID userId) {
        repository.deleteByUserId(userId);

        String token = new BigInteger(130, secureRandom).toString(32);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUserId(userId);
        resetToken.setExpireDate(LocalDateTime.now().plusMinutes(10));

        return repository.save(resetToken);
    }

    public UUID validateToken(String token) {
        PasswordResetToken resetToken = repository.findByToken(token)
                .orElseThrow(() ->
                        new EntityNotFoundException("Token not found or expired")
                );

        if (resetToken.getExpireDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token expired");
        }

        UUID userId = resetToken.getUserId();

        repository.delete(resetToken);

        return userId;
    }
}