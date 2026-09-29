package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.dto.UserResponse;
import com.example.smart_fuel_management_system.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private UUID aliceId;
    private UUID bobId;
    private UUID carolId;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        baseTime = LocalDateTime.of(2026, 9, 10, 10, 0);

        User alice = userRepository.save(buildUser("Alice Smith", "alice@example.com", baseTime));
        User bob = userRepository.save(buildUser("Bob Jones", "bob@example.com", baseTime.plusHours(1)));
        User carol = userRepository.save(buildUser("Carol White", "carol@example.com", baseTime.plusHours(2)));

        aliceId = alice.getId();
        bobId = bob.getId();
        carolId = carol.getId();
    }

    @Test
    void findFullNameById_shouldReturnUser_whenIdExists() {
        // given
        UUID searchId = aliceId;

        // when
        UserResponse result = userRepository.findFullNameById(searchId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(aliceId);
        assertThat(result.fullName()).isEqualTo("Alice Smith");
    }

    @Test
    void findFullNameById_shouldReturnNull_whenIdMissing() {
        // given
        UUID unknownId = UUID.randomUUID();

        // when
        UserResponse result = userRepository.findFullNameById(unknownId);

        // then
        assertThat(result).isNull();
    }

    @Test
    void findByFullNameContainingIgnoreCase_shouldReturnUser_whenFullNameMatchesExactly() {
        // given
        String exactName = "Alice Smith";

        // when
        List<UserResponse> result =
                userRepository.findByFullNameContainingIgnoreCase(exactName);

        // then
        assertThat(result)
                .hasSize(1)
                .extracting(UserResponse::fullName)
                .containsExactly("Alice Smith");
    }

    @Test
    void findByFullNameContainingIgnoreCase_shouldReturnEmpty_whenCaseDoesNotMatch() {
        // given
        String lowerCaseName = "alice smith";

        // when
        List<UserResponse> result =
                userRepository.findByFullNameContainingIgnoreCase(lowerCaseName);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByFullNameContainingIgnoreCase_shouldReturnEmpty_whenSearchIsPartial() {
        // given
        String partial = "Alice";

        // when
        List<UserResponse> result =
                userRepository.findByFullNameContainingIgnoreCase(partial);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByFullNameContainingIgnoreCase_shouldReturnEmptyList_whenNoMatch() {
        // given
        String unknownName = "Non Existent";

        // when
        List<UserResponse> result =
                userRepository.findByFullNameContainingIgnoreCase(unknownName);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByFullNameContainingIgnoreCase_shouldReturnPagedUsers_whenSearchMatches() {
        // given
        String search = "alice";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<User> result =
                userRepository.findByFullNameContainingIgnoreCase(search, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent())
                .extracting(User::getFullName)
                .containsExactly("Alice Smith");
    }

    @Test
    void findByFullNameContainingIgnoreCase_shouldBeCaseInsensitive_whenSearchMatches() {
        // given
        String upper = "ALICE";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<User> result =
                userRepository.findByFullNameContainingIgnoreCase(upper, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getFullName()).isEqualTo("Alice Smith");
    }

    @Test
    void findByFullNameContainingIgnoreCase_shouldReturnEmptyPage_whenNoMatch() {
        // given
        String search = "zzz";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<User> result =
                userRepository.findByFullNameContainingIgnoreCase(search, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void findByEmailContainingIgnoreCase_shouldReturnMatchingUsers_whenSearchMatches() {
        // given
        String search = "alice";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<User> result =
                userRepository.findByEmailContainingIgnoreCase(search, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void findByEmailContainingIgnoreCase_shouldBeCaseInsensitive_whenSearchMatches() {
        // given
        String upper = "ALICE";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<User> result =
                userRepository.findByEmailContainingIgnoreCase(upper, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findByEmailContainingIgnoreCase_shouldReturnAllUsers_whenSearchIsCommonDomain() {
        // given
        String search = "@example.com";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<User> result =
                userRepository.findByEmailContainingIgnoreCase(search, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    void findByEmailContainingIgnoreCase_shouldReturnEmptyPage_whenNoMatch() {
        // given
        String search = "nonexistent";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<User> result =
                userRepository.findByEmailContainingIgnoreCase(search, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void findAllByOrderByCreatedAtDesc_shouldReturnUsersNewestFirst() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<User> result =
                userRepository.findAllByOrderByCreatedAtDesc(pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent())
                .extracting(User::getId)
                .containsExactly(carolId, bobId, aliceId);
    }

    @Test
    void findAllByOrderByCreatedAtDesc_shouldRespectPagination() {
        // given
        Pageable firstPage = PageRequest.of(0, 2);

        // when
        Page<User> result =
                userRepository.findAllByOrderByCreatedAtDesc(firstPage);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(User::getId)
                .containsExactly(carolId, bobId);
    }

    @Test
    void findAllByOrderByCreatedAtDesc_shouldIgnorePageableSort_whenSortConflicts() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdAt"));

        // when
        Page<User> result =
                userRepository.findAllByOrderByCreatedAtDesc(pageable);

        // then
        assertThat(result.getContent())
                .extracting(User::getId)
                .containsExactly(carolId, bobId, aliceId);
    }

    private User buildUser(String fullName, String email, LocalDateTime createdAt) {
        return User.builder()
                .id(UUID.randomUUID())
                .fullName(fullName)
                .email(email)
                .createdAt(createdAt)
                .build();
    }
}