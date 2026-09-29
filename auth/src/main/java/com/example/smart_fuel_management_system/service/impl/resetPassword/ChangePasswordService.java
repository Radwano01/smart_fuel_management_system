package com.example.smart_fuel_management_system.service.impl.resetPassword;

import com.example.smart_fuel_management_system.dto.ChangePasswordRequest;
import com.example.smart_fuel_management_system.entity.Auth;
import com.example.smart_fuel_management_system.exceptions.BadRequestException;
import com.example.smart_fuel_management_system.repository.AuthRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ChangePasswordService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changePassword(ChangePasswordRequest request, UUID id) {

        Auth user = authRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("User does not found!"));

        if(!passwordEncoder.matches(request.currentPassword(), user.getPassword())){
            throw new BadRequestException("The current password does not match!");
        }

        if(passwordEncoder.matches(request.newPassword(), user.getPassword())){
            throw new BadRequestException("The password must be different from the current one!");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }
}
