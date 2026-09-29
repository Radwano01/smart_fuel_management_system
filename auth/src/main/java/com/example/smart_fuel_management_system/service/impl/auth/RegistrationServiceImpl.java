package com.example.smart_fuel_management_system.service.impl.auth;

import com.example.smart_fuel_management_system.dto.RegisterRequest;
import com.example.smart_fuel_management_system.dto.RegisterStationRequest;
import com.example.smart_fuel_management_system.entity.PendingUser;
import com.example.smart_fuel_management_system.enums.RoleType;
import com.example.smart_fuel_management_system.repository.AuthRepository;
import com.example.smart_fuel_management_system.service.PendingUserService;
import com.example.smart_fuel_management_system.service.RegisterService;
import com.example.smart_fuel_management_system.service.RegistrationOtpService;
import com.example.smart_fuel_management_system.service.impl.auth.client.StationClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegisterService {

    private final AuthRepository authRepository;
    private final RegistrationOtpService registrationOtpService;
    private final PendingUserService pendingUserService;
    private final PasswordEncoder passwordEncoder;
    private final StationClient stationClient;

    @Override
    public void register(RegisterRequest request) throws JsonProcessingException {

        pendingUserService.validateEmail(request.email());
        pendingUserService.validatePhone(request.phoneNumber());
        existsEmail(request.email());
        existsPhoneNumber(request.phoneNumber());

        PendingUser user = new PendingUser(
                UUID.randomUUID(),
                request.email(),
                request.fullName(),
                request.phoneNumber(),
                passwordEncoder.encode(request.password()),
                RoleType.USER,
                false,
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        pendingUserService.save(user);

        registrationOtpService.sendRegistrationOtp(user);
    }

    @Override
    public void registerStationEmployee(RegisterStationRequest request) throws JsonProcessingException {
        pendingUserService.validateEmail(request.email());
        pendingUserService.validatePhone(request.phoneNumber());
        existsEmail(request.email());
        existsPhoneNumber(request.phoneNumber());

        UUID employeeId = UUID.randomUUID();

        PendingUser stationUser = new PendingUser(
                employeeId,
                request.email(),
                request.fullName(),
                request.phoneNumber(),
                passwordEncoder.encode(request.password()),
                RoleType.STATION,
                false,
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        pendingUserService.save(stationUser);

        registrationOtpService.sendRegistrationOtp(stationUser);
    };

    private void existsEmail(String email){
        boolean exists = authRepository.existsByEmail(email);

        if(exists){
            throw new EntityExistsException("email already valid");
        }
    }

    private void existsPhoneNumber(String phoneNumber){
        boolean exists = authRepository.existsByPhoneNumber(phoneNumber);

        if(exists){
            throw new EntityExistsException("phone already valid");
        }
    }
}