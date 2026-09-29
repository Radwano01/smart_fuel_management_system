package com.example.smart_fuel_management_system.service.impl.auth;

import com.example.smart_fuel_management_system.dto.UpdateIdentifierRequest;
import com.example.smart_fuel_management_system.entity.Auth;
import com.example.smart_fuel_management_system.entity.PendingIdentifierChange;
import com.example.smart_fuel_management_system.enums.OtpType;
import com.example.smart_fuel_management_system.repository.AuthRepository;
import com.example.smart_fuel_management_system.service.IdentifierChangeService;
import com.example.smart_fuel_management_system.service.PendingIdentifierChangeService;
import com.example.smart_fuel_management_system.service.impl.IdentifierChangeOtpService;
import com.example.smart_fuel_management_system.service.impl.auth.client.UserClient;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdentifierChangeServiceImpl
        implements IdentifierChangeService {

    private final AuthRepository authRepository;
    private final PendingIdentifierChangeService pendingIdentifierChangeService;
    private final IdentifierChangeOtpService identifierChangeOtpService;
    private final UserClient userClient;

    @Override
    public UUID change(
            UUID authId,
            UpdateIdentifierRequest request) {

        if (pendingIdentifierChangeService
                .existsByAuthId(authId)) {

            throw new EntityExistsException(
                    "There is already a pending identifier change"
            );
        }

        Auth auth = authRepository.findById(authId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Employee/User not found"
                        )
                );

        String fullName =
                userClient.getUserById(auth.getId()).fullName();

        PendingIdentifierChange change =
                PendingIdentifierChange.builder()
                        .id(UUID.randomUUID())
                        .authId(auth.getId())
                        .fullName(fullName)
                        .type(request.type())
                        .newIdentifier(request.identifier())
                        .isVerified(false)
                        .createdAt(LocalDateTime.now())
                        .build();

        pendingIdentifierChangeService.save(change);

        identifierChangeOtpService.sendOtp(change);

        return change.getId();
    }

    @Transactional
    @Override
    public void verifyOtp(UUID changeId, String otp) {

        PendingIdentifierChange change =
                pendingIdentifierChangeService.findById(changeId);

        identifierChangeOtpService.verifyOtp(change.getId(), otp);

        userClient.updateEmail(change.getAuthId(), change.getNewIdentifier());

        Auth auth = authRepository.findById(change.getAuthId()).orElseThrow();

        if(change.getType() == OtpType.EMAIL){
            auth.setEmail(change.getNewIdentifier());
        }else{
            auth.setPhoneNumber(change.getNewIdentifier());
        }
    }

    @Override
    public void resendOtp(UUID changeId){
        identifierChangeOtpService.resendOtp(changeId);
    }
}