package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.entity.PasswordResetToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PasswordResetTokenRepositoryTest {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    private PasswordResetToken passwordResetToken;

    @BeforeEach
    void setUp() {
        passwordResetToken = new PasswordResetToken();
        passwordResetToken.setId(UUID.randomUUID());
        passwordResetToken.setToken("test-token");
        passwordResetToken.setUserId(UUID.randomUUID());

        passwordResetTokenRepository.save(passwordResetToken);
    }

    @Test
    void findByToken_shouldReturnToken_whenTokenExists() {
        // given

        // when
        Optional<PasswordResetToken> result =
                passwordResetTokenRepository.findByToken("test-token");

        // then
        assertThat(result)
                .isPresent()
                .get()
                .extracting(PasswordResetToken::getToken)
                .isEqualTo("test-token");
    }


    @Test
    void findByToken_shouldReturnEmpty_whenTokenDoesNotExist() {
        // given

        // when
        Optional<PasswordResetToken> result =
                passwordResetTokenRepository.findByToken("missing-token");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void deleteByUserId_shouldDeleteToken_whenUserIdExists() {
        // given
        UUID userId = passwordResetToken.getUserId();

        // when
        passwordResetTokenRepository.deleteByUserId(userId);

        // then
        Optional<PasswordResetToken> result =
                passwordResetTokenRepository.findByToken("test-token");

        assertThat(result).isEmpty();
    }

    @Test
    void deleteByUserId_shouldNotDeleteToken_whenUserIdDoesNotExist() {
        // given
        UUID differentUserId = UUID.randomUUID();

        // when
        passwordResetTokenRepository.deleteByUserId(differentUserId);

        // then
        Optional<PasswordResetToken> result =
                passwordResetTokenRepository.findByToken("test-token");

        assertThat(result).isPresent();
    }
}