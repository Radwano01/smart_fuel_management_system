package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.entity.Auth;
import com.example.smart_fuel_management_system.enums.AccountStatusType;
import com.example.smart_fuel_management_system.enums.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AuthRepositoryTest {

    @Autowired
    private AuthRepository authRepository;

    private Auth auth;

    @BeforeEach
    void setUp() {
        auth = new Auth();
        auth.setId(UUID.randomUUID());
        auth.setEmail("test@example.com");
        auth.setPhoneNumber("5551234567");
        auth.setRole(RoleType.ADMIN);
        auth.setAccountStatusType(AccountStatusType.ACTIVE);

        authRepository.save(auth);
    }

    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        // given

        // when
        boolean exists = authRepository.existsByEmail("test@example.com");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailDoesNotExist() {
        // given

        // when
        boolean exists = authRepository.existsByEmail("123@example.com");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByPhoneNumber_shouldReturnTrue_whenPhoneExists() {
        // given

        // when
        boolean exists = authRepository.existsByPhoneNumber("5551234567");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByPhoneNumber_shouldReturnFalse_whenPhoneDoesNotExist() {
        // given

        // when
        boolean exists = authRepository.existsByPhoneNumber("9999999999");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void findByEmail_shouldReturnAuth_whenEmailExists() {
        // given

        // when
        Optional<Auth> result = authRepository.findByEmail("test@example.com");

        // then
        assertThat(result)
                .isPresent()
                .get()
                .extracting(Auth::getEmail)
                .isEqualTo("test@example.com");
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {
        // given

        // when
        Optional<Auth> result = authRepository.findByEmail("missing@example.com");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByPhoneNumber_shouldReturnAuth_whenPhoneExists() {
        // given

        // when
        Optional<Auth> result = authRepository.findByPhoneNumber("5551234567");

        // then
        assertThat(result)
                .isPresent()
                .get()
                .extracting(Auth::getPhoneNumber)
                .isEqualTo("5551234567");
    }

    @Test
    void findByPhoneNumber_shouldReturnEmpty_whenPhoneDoesNotExist() {
        // given

        // when
        Optional<Auth> result = authRepository.findByPhoneNumber("9999999999");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findIdByEmail_shouldReturnId_whenEmailExists() {
        // given

        // when
        UUID id = authRepository.findIdByEmail("test@example.com");

        // then
        assertThat(id).isEqualTo(auth.getId());
    }

    @Test
    void findIdByEmail_shouldReturnNull_whenEmailDoesNotExist() {
        // given

        // when
        UUID id = authRepository.findIdByEmail("missing@example.com");

        // then
        assertThat(id).isNull();
    }

    @Test
    void findByAccountStatusTypeAndRole_shouldReturnAuth_whenStatusAndRoleMatch() {
        // given

        // when
        Page<Auth> result = authRepository.findByAccountStatusTypeAndRole(
                AccountStatusType.ACTIVE,
                RoleType.ADMIN,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent())
                .hasSize(1)
                .first()
                .extracting(Auth::getEmail)
                .isEqualTo("test@example.com");
    }

    @Test
    void findByAccountStatusTypeAndRole_shouldReturnEmpty_whenStatusDoesNotMatch() {
        // given

        // when
        Page<Auth> result = authRepository.findByAccountStatusTypeAndRole(
                AccountStatusType.INACTIVE,
                RoleType.ADMIN,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findByAccountStatusTypeAndRole_shouldReturnEmpty_whenRoleDoesNotMatch() {
        // given

        // when
        Page<Auth> result = authRepository.findByAccountStatusTypeAndRole(
                AccountStatusType.ACTIVE,
                RoleType.USER,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findAllByRole_shouldReturnAuth_whenRoleMatches() {
        // given

        // when
        Page<Auth> result = authRepository.findAllByRole(
                RoleType.ADMIN,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent())
                .hasSize(1)
                .first()
                .extracting(Auth::getEmail)
                .isEqualTo("test@example.com");
    }

    @Test
    void findAllByRole_shouldReturnEmpty_whenRoleDoesNotMatch() {
        // given

        // when
        Page<Auth> result = authRepository.findAllByRole(
                RoleType.USER,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findAllByIdInAndRole_shouldReturnAuth_whenIdAndRoleMatch() {
        // given
        List<UUID> userIds = List.of(auth.getId());

        // when
        List<Auth> result = authRepository.findAllByIdInAndRole(
                userIds,
                RoleType.ADMIN
        );

        // then
        assertThat(result)
                .hasSize(1)
                .first()
                .extracting(Auth::getId)
                .isEqualTo(auth.getId());
    }

    @Test
    void findAllByIdInAndRole_shouldReturnEmpty_whenIdDoesNotExist() {
        // given
        List<UUID> userIds = List.of(UUID.randomUUID());

        // when
        List<Auth> result = authRepository.findAllByIdInAndRole(
                userIds,
                RoleType.ADMIN
        );

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findAllByIdInAndRole_shouldReturnEmpty_whenRoleDoesNotMatch() {
        // given
        List<UUID> userIds = List.of(auth.getId());

        // when
        List<Auth> result = authRepository.findAllByIdInAndRole(
                userIds,
                RoleType.USER
        );

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void searchByEmailAndRole_shouldReturnAuth_whenEmailMatches() {
        // given

        // when
        Page<Auth> result = authRepository.searchByEmailAndRole(
                "test",
                RoleType.ADMIN,
                null,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent())
                .hasSize(1)
                .first()
                .extracting(Auth::getEmail)
                .isEqualTo("test@example.com");
    }

    @Test
    void searchByEmailAndRole_shouldReturnEmpty_whenEmailDoesNotMatch() {
        // given

        // when
        Page<Auth> result = authRepository.searchByEmailAndRole(
                "missing",
                RoleType.ADMIN,
                null,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void searchByEmailAndRole_shouldReturnEmpty_whenRoleDoesNotMatch() {
        // given

        // when
        Page<Auth> result = authRepository.searchByEmailAndRole(
                "test",
                RoleType.USER,
                null,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void searchByEmailAndRole_shouldReturnAuth_whenStatusMatches() {
        // given

        // when
        Page<Auth> result = authRepository.searchByEmailAndRole(
                "test",
                RoleType.ADMIN,
                AccountStatusType.ACTIVE,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent())
                .hasSize(1)
                .first()
                .extracting(Auth::getEmail)
                .isEqualTo("test@example.com");
    }

    @Test
    void searchByEmailAndRole_shouldReturnEmpty_whenStatusDoesNotMatch() {
        // given

        // when
        Page<Auth> result = authRepository.searchByEmailAndRole(
                "test",
                RoleType.ADMIN,
                AccountStatusType.INACTIVE,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void searchByIdsAndRole_shouldReturnAuth_whenIdMatches() {
        // given
        List<UUID> userIds = List.of(auth.getId());

        // when
        Page<Auth> result = authRepository.searchByIdsAndRole(
                userIds,
                RoleType.ADMIN,
                null,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent())
                .hasSize(1)
                .first()
                .extracting(Auth::getId)
                .isEqualTo(auth.getId());
    }

    @Test
    void searchByIdsAndRole_shouldReturnEmpty_whenIdDoesNotExist() {
        // given
        List<UUID> userIds = List.of(UUID.randomUUID());

        // when
        Page<Auth> result = authRepository.searchByIdsAndRole(
                userIds,
                RoleType.ADMIN,
                null,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void searchByIdsAndRole_shouldReturnEmpty_whenRoleDoesNotMatch() {
        // given
        List<UUID> userIds = List.of(auth.getId());

        // when
        Page<Auth> result = authRepository.searchByIdsAndRole(
                userIds,
                RoleType.USER,
                null,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void searchByIdsAndRole_shouldReturnEmpty_whenStatusDoesNotMatch() {
        // given
        List<UUID> userIds = List.of(auth.getId());

        // when
        Page<Auth> result = authRepository.searchByIdsAndRole(
                userIds,
                RoleType.ADMIN,
                AccountStatusType.INACTIVE,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void searchByPhoneNumberAndRole_shouldReturnAuth_whenPhoneNumberMatches() {
        // given

        // when
        Page<Auth> result = authRepository.searchByPhoneNumberAndRole(
                "555123",
                RoleType.ADMIN,
                null,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent())
                .hasSize(1)
                .first()
                .extracting(Auth::getPhoneNumber)
                .isEqualTo("5551234567");
    }

    @Test
    void searchByPhoneNumberAndRole_shouldReturnEmpty_whenPhoneNumberDoesNotMatch() {
        // given

        // when
        Page<Auth> result = authRepository.searchByPhoneNumberAndRole(
                "999999",
                RoleType.ADMIN,
                null,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void searchByPhoneNumberAndRole_shouldReturnEmpty_whenRoleDoesNotMatch() {
        // given

        // when
        Page<Auth> result = authRepository.searchByPhoneNumberAndRole(
                "555123",
                RoleType.USER,
                null,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void searchByPhoneNumberAndRole_shouldReturnAuth_whenStatusMatches() {
        // given

        // when
        Page<Auth> result = authRepository.searchByPhoneNumberAndRole(
                "555123",
                RoleType.ADMIN,
                AccountStatusType.ACTIVE,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent())
                .hasSize(1)
                .first()
                .extracting(Auth::getPhoneNumber)
                .isEqualTo("5551234567");
    }

    @Test
    void searchByPhoneNumberAndRole_shouldReturnEmpty_whenStatusDoesNotMatch() {
        // given

        // when
        Page<Auth> result = authRepository.searchByPhoneNumberAndRole(
                "555123",
                RoleType.ADMIN,
                AccountStatusType.INACTIVE,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findByIdAndRole_shouldReturnAuth_whenIdAndRoleMatch() {
        // given

        // when
        Optional<Auth> result = authRepository.findByIdAndRole(
                auth.getId(),
                RoleType.ADMIN
        );

        // then
        assertThat(result)
                .isPresent()
                .get()
                .extracting(Auth::getId)
                .isEqualTo(auth.getId());
    }

    @Test
    void findByIdAndRole_shouldReturnEmpty_whenIdDoesNotExist() {
        // given

        // when
        Optional<Auth> result = authRepository.findByIdAndRole(
                UUID.randomUUID(),
                RoleType.ADMIN
        );

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByIdAndRole_shouldReturnEmpty_whenRoleDoesNotMatch() {
        // given

        // when
        Optional<Auth> result = authRepository.findByIdAndRole(
                auth.getId(),
                RoleType.USER
        );

        // then
        assertThat(result).isEmpty();
    }
}