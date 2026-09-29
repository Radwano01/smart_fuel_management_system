package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.AuthUserResponse;
import com.example.smart_fuel_management_system.dto.SaveUser;
import com.example.smart_fuel_management_system.dto.UpdateDTO;
import com.example.smart_fuel_management_system.dto.UserDTO;
import com.example.smart_fuel_management_system.dto.UserDashboardSummaryResponse;
import com.example.smart_fuel_management_system.dto.UserResponse;
import com.example.smart_fuel_management_system.dto.UserResponseToPaymentService;
import com.example.smart_fuel_management_system.dto.UserSummaryResponse;
import com.example.smart_fuel_management_system.entity.User;
import com.example.smart_fuel_management_system.enums.AccountSearchType;
import com.example.smart_fuel_management_system.enums.AccountStatusType;
import com.example.smart_fuel_management_system.exceptions.BadRequestException;
import com.example.smart_fuel_management_system.repository.UserRepository;
import com.example.smart_fuel_management_system.service.impl.client.AuthClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        baseTime = LocalDateTime.of(2026, 9, 10, 10, 0);
    }

    private User buildUser(UUID id, String fullName, String email) {
        return new User(id, fullName, email, baseTime, baseTime);
    }

    private AuthUserResponse buildAuthUser(UUID id) {
        return new AuthUserResponse(
                id,
                "+90 555 000 0000",
                AccountStatusType.ACTIVE,
                baseTime
        );
    }
    @Test
    void create_shouldSaveUser_whenPayloadIsValid() throws Exception {
        // given
        String payload = "{\"id\":\"" + userId + "\"}";
        SaveUser parsed = new SaveUser(
                userId, "Alice Smith", "alice@example.com", baseTime, baseTime);
        when(objectMapper.readValue(payload, SaveUser.class)).thenReturn(parsed);

        // when
        userService.create(payload);

        // then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(userId);
        assertThat(saved.getFullName()).isEqualTo("Alice Smith");
        assertThat(saved.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void create_shouldThrow_whenPayloadCannotBeParsed() throws Exception {
        // given
        String payload = "not-json";
        JsonProcessingException failure = mock(JsonProcessingException.class);
        when(objectMapper.readValue(payload, SaveUser.class)).thenThrow(failure);

        // when / then
        assertThatThrownBy(() -> userService.create(payload))
                .isSameAs(failure);

        verify(userRepository, never()).save(any(User.class));
    }

    // ---------------------------------------------------------------------
    // getUser
    // ---------------------------------------------------------------------

    @Test
    void getUser_shouldReturnUserDTO_whenUserExists() {
        // given
        User user = buildUser(userId, "Alice Smith", "alice@example.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        UserDTO result = userService.getUser(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.fullName()).isEqualTo("Alice Smith");
    }

    @Test
    void getUser_shouldThrow_whenUserDoesNotExist() {
        // given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.getUser(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User id is not found");
    }

    // ---------------------------------------------------------------------
    // updateUser
    // ---------------------------------------------------------------------

    @Test
    void updateUser_shouldChangeFullName_whenNewValueDiffers() {
        // given
        User user = buildUser(userId, "Old Name", "alice@example.com");
        UpdateDTO update = new UpdateDTO("New Name");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        userService.updateUser(userId, update);

        // then
        assertThat(user.getFullName()).isEqualTo("New Name");
    }

    @Test
    void updateUser_shouldKeepFullName_whenNewValueIsNull() {
        // given
        User user = buildUser(userId, "Old Name", "alice@example.com");
        UpdateDTO update = new UpdateDTO(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        userService.updateUser(userId, update);

        // then
        assertThat(user.getFullName()).isEqualTo("Old Name");
    }

    @Test
    void updateUser_shouldKeepFullName_whenNewValueIsSame() {
        // given
        User user = buildUser(userId, "Same Name", "alice@example.com");
        UpdateDTO update = new UpdateDTO("Same Name");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        userService.updateUser(userId, update);

        // then
        assertThat(user.getFullName()).isEqualTo("Same Name");
    }

    @Test
    void updateUser_shouldThrow_whenUserDoesNotExist() {
        // given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.updateUser(userId, new UpdateDTO("x")))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ---------------------------------------------------------------------
    // getUserResponse
    // ---------------------------------------------------------------------

    @Test
    void getUserResponse_shouldDelegateToRepository() {
        // given
        UserResponse expected = new UserResponse(userId, "Alice Smith");
        when(userRepository.findFullNameById(userId)).thenReturn(expected);

        // when
        UserResponse result = userService.getUserResponse(userId);

        // then
        assertThat(result).isSameAs(expected);
        verify(userRepository).findFullNameById(userId);
    }

    // ---------------------------------------------------------------------
    // getUserForPayment
    // ---------------------------------------------------------------------

    @Test
    void getUserForPayment_shouldReturnPaymentDTO_whenUserExists() {
        // given
        User user = buildUser(userId, "Alice Smith", "alice@example.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        UserResponseToPaymentService result = userService.getUserForPayment(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.fullName()).isEqualTo("Alice Smith");
        assertThat(result.email()).isEqualTo("alice@example.com");
    }

    @Test
    void getUserForPayment_shouldThrow_whenUserDoesNotExist() {
        // given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.getUserForPayment(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    // ---------------------------------------------------------------------
    // getDashboardSummary
    // ---------------------------------------------------------------------

    @Test
    void getDashboardSummary_shouldReturnCountFromRepository() {
        // given
        when(userRepository.count()).thenReturn(42L);

        // when
        UserDashboardSummaryResponse result = userService.getDashboardSummary();

        // then
        assertThat(result.count()).isEqualTo(42L);
    }

    // ---------------------------------------------------------------------
    // updateEmail
    // ---------------------------------------------------------------------

    @Test
    void updateEmail_shouldSetEmail_whenUserExists() {
        // given
        User user = buildUser(userId, "Alice Smith", "old@example.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        userService.updateEmail(userId, "new@example.com");

        // then
        assertThat(user.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void updateEmail_shouldThrow_whenUserDoesNotExist() {
        // given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.updateEmail(userId, "x@y.com"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ---------------------------------------------------------------------
    // getUsersByIds
    // ---------------------------------------------------------------------

    @Test
    void getUsersByIds_shouldReturnMappedResponses_whenIdsExist() {
        // given
        UUID secondId = UUID.randomUUID();
        User first = buildUser(userId, "Alice", "a@example.com");
        User second = buildUser(secondId, "Bob", "b@example.com");
        when(userRepository.findAllById(List.of(userId, secondId)))
                .thenReturn(List.of(first, second));

        // when
        List<UserResponse> result =
                userService.getUsersByIds(List.of(userId, secondId));

        // then
        assertThat(result)
                .hasSize(2)
                .extracting(UserResponse::id)
                .containsExactlyInAnyOrder(userId, secondId);
    }

    @Test
    void getUsersByIds_shouldReturnEmptyList_whenRepositoryReturnsEmpty() {
        // given
        when(userRepository.findAllById(anyList())).thenReturn(List.of());

        // when
        List<UserResponse> result =
                userService.getUsersByIds(List.of(userId));

        // then
        assertThat(result).isEmpty();
    }

    // ---------------------------------------------------------------------
    // getUsersByFullName
    // ---------------------------------------------------------------------

    @Test
    void getUsersByFullName_shouldDelegateToRepository() {
        // given
        UserResponse expected = new UserResponse(userId, "Alice Smith");
        when(userRepository.findByFullNameContainingIgnoreCase("Alice"))
                .thenReturn(List.of(expected));

        // when
        List<UserResponse> result = userService.getUsersByFullName("Alice");

        // then
        assertThat(result).containsExactly(expected);
    }

    // ---------------------------------------------------------------------
    // getUsersBySearch
    // ---------------------------------------------------------------------

    @Test
    void getUsersBySearch_shouldReturnLatestUsers_whenSearchTypeIsNull() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAllByOrderByCreatedAtDesc(pageable))
                .thenReturn(new PageImpl<>(List.of()));

        // when
        Page<UserSummaryResponse> result = userService.getUsersBySearch(
                null, "anything", AccountStatusType.ACTIVE, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        verify(userRepository).findAllByOrderByCreatedAtDesc(pageable);
    }

    @Test
    void getUsersBySearch_shouldReturnLatestUsers_whenSearchAndStatusAreNull() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAllByOrderByCreatedAtDesc(pageable))
                .thenReturn(new PageImpl<>(List.of()));

        // when
        Page<UserSummaryResponse> result = userService.getUsersBySearch(
                AccountSearchType.NAME, null, null, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        verify(userRepository).findAllByOrderByCreatedAtDesc(pageable);
    }

    @Test
    void getUsersBySearch_shouldSearchByName_whenSearchTypeIsName() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        User user = buildUser(userId, "Alice", "a@example.com");
        when(userRepository.findByFullNameContainingIgnoreCase("Alice", pageable))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));
        when(authClient.getUsers(List.of(userId)))
                .thenReturn(List.of(buildAuthUser(userId)));

        // when
        Page<UserSummaryResponse> result = userService.getUsersBySearch(
                AccountSearchType.NAME, "Alice", null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(userRepository).findByFullNameContainingIgnoreCase("Alice", pageable);
    }

    @Test
    void getUsersBySearch_shouldSearchByEmail_whenSearchTypeIsEmail() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        User user = buildUser(userId, "Alice", "alice@example.com");
        when(userRepository.findByEmailContainingIgnoreCase("alice", pageable))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));
        when(authClient.getUsers(List.of(userId)))
                .thenReturn(List.of(buildAuthUser(userId)));

        // when
        Page<UserSummaryResponse> result = userService.getUsersBySearch(
                AccountSearchType.EMAIL, "alice", null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(userRepository).findByEmailContainingIgnoreCase("alice", pageable);
    }

    @Test
    void getUsersBySearch_shouldSearchByPhone_whenSearchTypeIsPhone() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        AuthUserResponse auth = buildAuthUser(userId);
        when(authClient.searchByPhone("555", pageable)).thenReturn(List.of(auth));
        when(userRepository.findAllById(List.of(userId)))
                .thenReturn(List.of(buildUser(userId, "Alice", "a@example.com")));

        // when
        Page<UserSummaryResponse> result = userService.getUsersBySearch(
                AccountSearchType.PHONE, "555", null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).phoneNumber())
                .isEqualTo("+90 555 000 0000");
        verify(authClient).searchByPhone("555", pageable);
    }

    @Test
    void getUsersBySearch_shouldFilterByStatus_whenSearchTypeIsAccountStatusType() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        AuthUserResponse auth = buildAuthUser(userId);
        when(authClient.getUsersByStatus(AccountStatusType.ACTIVE, pageable))
                .thenReturn(List.of(auth));
        when(userRepository.findAllById(List.of(userId)))
                .thenReturn(List.of(buildUser(userId, "Alice", "a@example.com")));

        // when
        Page<UserSummaryResponse> result = userService.getUsersBySearch(
                AccountSearchType.ACCOUNT_STATUS_TYPE, "any", AccountStatusType.ACTIVE, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(authClient).getUsersByStatus(AccountStatusType.ACTIVE, pageable);
    }

    @Test
    void getUsersBySearch_shouldThrow_whenSearchTypeIsNameAndSearchIsNull() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when / then
        assertThatThrownBy(() -> userService.getUsersBySearch(
                AccountSearchType.NAME, null, AccountStatusType.ACTIVE, pageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Search value cannot be empty");
    }

    @Test
    void getUsersBySearch_shouldThrow_whenSearchTypeIsEmailAndSearchIsBlank() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when / then
        assertThatThrownBy(() -> userService.getUsersBySearch(
                AccountSearchType.EMAIL, "   ", AccountStatusType.ACTIVE, pageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Search value cannot be empty");
    }

    @Test
    void getUsersBySearch_shouldThrow_whenSearchTypeIsPhoneAndSearchIsNull() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when / then
        assertThatThrownBy(() -> userService.getUsersBySearch(
                AccountSearchType.PHONE, null, AccountStatusType.ACTIVE, pageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Search value cannot be empty");
    }

    @Test
    void getUsersBySearch_shouldThrow_whenSearchTypeIsAccountStatusTypeAndStatusIsNull() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when / then
        assertThatThrownBy(() -> userService.getUsersBySearch(
                AccountSearchType.ACCOUNT_STATUS_TYPE, "any", null, pageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Account status cannot be empty");
    }

    @Test
    void getUsersBySearch_shouldFilterOutUsersMissingFromAuthResponse() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        UUID otherUserId = UUID.randomUUID();
        User user = buildUser(userId, "Alice", "a@example.com");
        when(userRepository.findAllByOrderByCreatedAtDesc(pageable))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));
        when(authClient.getUsers(List.of(userId))).thenReturn(List.of());

        // when
        Page<UserSummaryResponse> result = userService.getUsersBySearch(
                null, null, null, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
    }
}