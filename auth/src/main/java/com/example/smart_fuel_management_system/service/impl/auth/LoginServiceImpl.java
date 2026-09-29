package com.example.smart_fuel_management_system.service.impl.auth;

import com.example.smart_fuel_management_system.dto.*;
import com.example.smart_fuel_management_system.entity.Auth;
import com.example.smart_fuel_management_system.enums.AccountStatusType;
import com.example.smart_fuel_management_system.enums.LoginMethodType;
import com.example.smart_fuel_management_system.exceptions.BadRequestException;
import com.example.smart_fuel_management_system.repository.AuthRepository;
import com.example.smart_fuel_management_system.security.JWTGenerator;
import com.example.smart_fuel_management_system.service.LoginService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTGenerator jwtGenerator;

    @Override
    public LoginResponse userLogin(LoginRequest request) {
        return authenticateAndLogin(request, "USER");
    }

    @Override
    public LoginResponse adminLogin(LoginRequest request) {
        return authenticateAndLogin(request, "ADMIN");
    }

    @Override
    public LoginResponse stationLogin(LoginRequest request) {
        return authenticateAndLogin(request, "STATION");
    }

    private LoginResponse authenticateAndLogin(
            LoginRequest request,
            String requiredRole) {

        Auth user = declareMethodTypeAndReturnUser(request);

        if(user.getAccountStatusType() != AccountStatusType.ACTIVE){
            throw new BadRequestException("your account is: " + user.getAccountStatusType());
        }

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword())) {

            throw new BadRequestException("Invalid credentials");
        }

        String actualRole = user.getRole().toString();

        // The role comes from the database.
        // The required role comes from the endpoint.
        if (!actualRole.equals(requiredRole)) {
            throw new BadRequestException(
                    "You are not authorized for this application"
            );
        }

        return createLoginResponse(user);
    }

    private LoginResponse createLoginResponse(Auth user) {

        String role = user.getRole().toString();

        String accessToken =
                jwtGenerator.generateToken(
                        user.getId(),
                        role
                );

        String refreshToken =
                jwtGenerator.generateRefreshToken(
                        user.getId(),
                        role
                );

        return new LoginResponse(
                accessToken,
                refreshToken
        );
    }

    @Override
    public RefreshTokenResponse refreshToken(String refreshToken) {

        if (!jwtGenerator.validateToken(refreshToken)) {
            throw new BadRequestException("Refresh token is expired!");
        }

        UUID userId = jwtGenerator.extractUserId(refreshToken);
        String role = jwtGenerator.extractRole(refreshToken);

        String accessToken =
                jwtGenerator.generateToken(userId, role);

        return new RefreshTokenResponse(accessToken);
    }

    private Auth declareMethodTypeAndReturnUser(
            LoginRequest request) {

        if (request.type() == LoginMethodType.EMAIL) {

            return authRepository.findByEmail(request.identifier())
                    .orElseThrow(() ->
                            new EntityNotFoundException(
                                    "User not found"
                            ));
        }

        return authRepository.findByPhoneNumber(request.identifier())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "User not found"
                        ));
    }
}