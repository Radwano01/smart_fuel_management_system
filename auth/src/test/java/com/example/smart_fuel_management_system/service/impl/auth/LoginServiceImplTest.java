package com.example.smart_fuel_management_system.service.impl.auth;

import com.example.smart_fuel_management_system.dto.LoginRequest;
import com.example.smart_fuel_management_system.dto.LoginResponse;
import com.example.smart_fuel_management_system.dto.RefreshTokenResponse;
import com.example.smart_fuel_management_system.entity.Auth;
import com.example.smart_fuel_management_system.enums.AccountStatusType;
import com.example.smart_fuel_management_system.enums.LoginMethodType;
import com.example.smart_fuel_management_system.enums.RoleType;
import com.example.smart_fuel_management_system.exceptions.BadRequestException;
import com.example.smart_fuel_management_system.security.JWTGenerator;
import com.example.smart_fuel_management_system.repository.AuthRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JWTGenerator jwtGenerator;

    @InjectMocks
    private LoginServiceImpl loginService;

    private Auth user;

    @BeforeEach
    void setUp() {
        user = new Auth();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPhoneNumber("5551234567");
        user.setPassword("hashed-password");
        user.setRole(RoleType.USER);
        user.setAccountStatusType(AccountStatusType.ACTIVE);
    }

    @Test
    void userLogin_shouldReturnTokens_whenCredentialsAreValid() {

        // given
        LoginRequest request = new LoginRequest(
                LoginMethodType.EMAIL,
                "test@example.com",
                "password"
        );

        when(authRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password",
                "hashed-password"
        )).thenReturn(true);

        when(jwtGenerator.generateToken(user.getId(), "USER"))
                .thenReturn("access-token");

        when(jwtGenerator.generateRefreshToken(user.getId(), "USER"))
                .thenReturn("refresh-token");

        // when
        LoginResponse result = loginService.userLogin(request);

        // then
        assertThat(result.token())
                .isEqualTo("access-token");

        assertThat(result.refreshToken())
                .isEqualTo("refresh-token");

        verify(authRepository).findByEmail("test@example.com");
        verify(passwordEncoder)
                .matches("password", "hashed-password");

        verify(jwtGenerator)
                .generateToken(user.getId(), "USER");

        verify(jwtGenerator)
                .generateRefreshToken(user.getId(), "USER");
    }

    @Test
    void userLogin_shouldReturnTokens_whenPhoneNumberIsValid() {

        // given
        LoginRequest request = new LoginRequest(
                LoginMethodType.PHONE,
                "5551234567",
                "password"
        );

        when(authRepository.findByPhoneNumber("5551234567"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password",
                "hashed-password"
        )).thenReturn(true);

        when(jwtGenerator.generateToken(user.getId(), "USER"))
                .thenReturn("access-token");

        when(jwtGenerator.generateRefreshToken(user.getId(), "USER"))
                .thenReturn("refresh-token");

        // when
        LoginResponse result = loginService.userLogin(request);

        // then
        assertThat(result.token())
                .isEqualTo("access-token");

        assertThat(result.refreshToken())
                .isEqualTo("refresh-token");

        verify(authRepository)
                .findByPhoneNumber("5551234567");
    }

    @Test
    void userLogin_shouldThrowException_whenUserNotFoundByEmail() {

        // given
        LoginRequest request = new LoginRequest(
                LoginMethodType.EMAIL,
                "missing@example.com",
                "password"
        );

        when(authRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> loginService.userLogin(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void userLogin_shouldThrowException_whenUserNotFoundByPhone() {

        // given
        LoginRequest request = new LoginRequest(
                LoginMethodType.PHONE,
                "9999999999",
                "password"
        );

        when(authRepository.findByPhoneNumber("9999999999"))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> loginService.userLogin(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void userLogin_shouldThrowException_whenAccountIsNotActive() {

        // given
        user.setAccountStatusType(AccountStatusType.BLOCKED);

        LoginRequest request = new LoginRequest(
                LoginMethodType.EMAIL,
                "test@example.com",
                "password"
        );

        when(authRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        // when / then
        assertThatThrownBy(() -> loginService.userLogin(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("your account is: BLOCKED");

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());
    }

    @Test
    void userLogin_shouldThrowException_whenPasswordIsWrong() {

        // given
        LoginRequest request = new LoginRequest(
                LoginMethodType.EMAIL,
                "test@example.com",
                "wrong-password"
        );

        when(authRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrong-password",
                "hashed-password"
        )).thenReturn(false);

        // when / then
        assertThatThrownBy(() -> loginService.userLogin(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid credentials");

        verify(jwtGenerator, never())
                .generateToken(any(UUID.class), anyString());

        verify(jwtGenerator, never())
                .generateRefreshToken(any(UUID.class), anyString());
    }

    @Test
    void userLogin_shouldThrowException_whenRoleDoesNotMatch() {

        // given
        user.setRole(RoleType.ADMIN);

        LoginRequest request = new LoginRequest(
                LoginMethodType.EMAIL,
                "test@example.com",
                "password"
        );

        when(authRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password",
                "hashed-password"
        )).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> loginService.userLogin(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("You are not authorized for this application");

        verify(jwtGenerator, never())
                .generateToken(any(UUID.class), anyString());

        verify(jwtGenerator, never())
                .generateRefreshToken(any(UUID.class), anyString());
    }

    @Test
    void adminLogin_shouldReturnTokens_whenAdminCredentialsAreValid() {

        // given
        user.setRole(RoleType.ADMIN);

        LoginRequest request = new LoginRequest(
                LoginMethodType.EMAIL,
                "test@example.com",
                "password"
        );

        when(authRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password",
                "hashed-password"
        )).thenReturn(true);

        when(jwtGenerator.generateToken(user.getId(), "ADMIN"))
                .thenReturn("access-token");

        when(jwtGenerator.generateRefreshToken(user.getId(), "ADMIN"))
                .thenReturn("refresh-token");

        // when
        LoginResponse result = loginService.adminLogin(request);

        // then
        assertThat(result.token())
                .isEqualTo("access-token");

        assertThat(result.refreshToken())
                .isEqualTo("refresh-token");

        verify(jwtGenerator)
                .generateToken(user.getId(), "ADMIN");

        verify(jwtGenerator)
                .generateRefreshToken(user.getId(), "ADMIN");
    }

    @Test
    void stationLogin_shouldReturnTokens_whenStationCredentialsAreValid() {

        // given
        user.setRole(RoleType.STATION);

        LoginRequest request = new LoginRequest(
                LoginMethodType.EMAIL,
                "test@example.com",
                "password"
        );

        when(authRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password",
                "hashed-password"
        )).thenReturn(true);

        when(jwtGenerator.generateToken(user.getId(), "STATION"))
                .thenReturn("access-token");

        when(jwtGenerator.generateRefreshToken(user.getId(), "STATION"))
                .thenReturn("refresh-token");

        // when
        LoginResponse result = loginService.stationLogin(request);

        // then
        assertThat(result.token())
                .isEqualTo("access-token");

        assertThat(result.refreshToken())
                .isEqualTo("refresh-token");

        verify(jwtGenerator)
                .generateToken(user.getId(), "STATION");

        verify(jwtGenerator)
                .generateRefreshToken(user.getId(), "STATION");
    }

    @Test
    void adminLogin_shouldThrowException_whenUserRoleIsNotAdmin() {

        // given
        user.setRole(RoleType.USER);

        LoginRequest request = new LoginRequest(
                LoginMethodType.EMAIL,
                "test@example.com",
                "password"
        );

        when(authRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password",
                "hashed-password"
        )).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> loginService.adminLogin(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("You are not authorized for this application");
    }

    @Test
    void stationLogin_shouldThrowException_whenUserRoleIsNotStation() {

        // given
        user.setRole(RoleType.USER);

        LoginRequest request = new LoginRequest(
                LoginMethodType.EMAIL,
                "test@example.com",
                "password"
        );

        when(authRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password",
                "hashed-password"
        )).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> loginService.stationLogin(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("You are not authorized for this application");
    }

    @Test
    void refreshToken_shouldReturnNewAccessToken_whenRefreshTokenIsValid() {

        // given
        String refreshToken = "refresh-token";
        UUID userId = user.getId();

        when(jwtGenerator.validateToken(refreshToken))
                .thenReturn(true);

        when(jwtGenerator.extractUserId(refreshToken))
                .thenReturn(userId);

        when(jwtGenerator.extractRole(refreshToken))
                .thenReturn("USER");

        when(jwtGenerator.generateToken(userId, "USER"))
                .thenReturn("new-access-token");

        // when
        RefreshTokenResponse result =
                loginService.refreshToken(refreshToken);

        // then
        assertThat(result.accessToken())
                .isEqualTo("new-access-token");

        verify(jwtGenerator)
                .validateToken(refreshToken);

        verify(jwtGenerator)
                .extractUserId(refreshToken);

        verify(jwtGenerator)
                .extractRole(refreshToken);

        verify(jwtGenerator)
                .generateToken(userId, "USER");
    }

    @Test
    void refreshToken_shouldThrowException_whenRefreshTokenIsInvalid() {

        // given
        String refreshToken = "expired-token";

        when(jwtGenerator.validateToken(refreshToken))
                .thenReturn(false);

        // when / then
        assertThatThrownBy(() ->
                loginService.refreshToken(refreshToken)
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Refresh token is expired!");

        verify(jwtGenerator).validateToken(refreshToken);

        verify(jwtGenerator, never())
                .extractUserId(anyString());

        verify(jwtGenerator, never())
                .extractRole(anyString());

        verify(jwtGenerator, never())
                .generateToken(any(UUID.class), anyString());
    }
}