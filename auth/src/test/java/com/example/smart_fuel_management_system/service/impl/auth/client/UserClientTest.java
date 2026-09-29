package com.example.smart_fuel_management_system.service.impl.auth.client;

import com.example.smart_fuel_management_system.dto.UserResponse;
import com.example.smart_fuel_management_system.exceptions.ServiceUnavailableException;
import com.example.smart_fuel_management_system.security.JWTGenerator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JWTGenerator jwtGenerator;

    @InjectMocks
    private UserClient userClient;

    @Test
    void getUserById_shouldReturnUser_whenRequestSucceeds() {

        // given
        UUID userId = UUID.randomUUID();

        UserResponse user =
                new UserResponse(userId, "Radwan Rahmoun");

        when(jwtGenerator.generateServiceToken(
                "auth-service",
                "user-service",
                "user.internal.user.read"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                eq("http://USER/api/v1/internal/users/{id}"),
                eq(org.springframework.http.HttpMethod.GET),
                any(),
                eq(UserResponse.class),
                eq(userId)
        )).thenReturn(ResponseEntity.ok(user));

        // when
        UserResponse result =
                userClient.getUserById(userId);

        // then
        assertThat(result)
                .isEqualTo(user);
    }

    @Test
    void getUserById_shouldThrowEntityNotFound_whenUserDoesNotExist() {

        // given
        UUID userId = UUID.randomUUID();

        when(jwtGenerator.generateServiceToken(
                "auth-service",
                "user-service",
                "user.internal.user.read"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                anyString(),
                eq(org.springframework.http.HttpMethod.GET),
                any(),
                eq(UserResponse.class),
                eq(userId)
        )).thenThrow(
                HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND,
                        "Not Found",
                        null,
                        null,
                        null
                )
        );

        // when & then
        assertThatThrownBy(() ->
                userClient.getUserById(userId)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found: " + userId);
    }

    @Test
    void getUserById_shouldThrowServiceUnavailable_whenRequestFails() {

        // given
        UUID userId = UUID.randomUUID();

        when(jwtGenerator.generateServiceToken(
                "auth-service",
                "user-service",
                "user.internal.user.read"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                anyString(),
                eq(org.springframework.http.HttpMethod.GET),
                any(),
                eq(UserResponse.class),
                eq(userId)
        )).thenThrow(
                new RestClientException("Connection failed")
        );

        // when & then
        assertThatThrownBy(() ->
                userClient.getUserById(userId)
        )
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessage("User service is unavailable");
    }

    @Test
    void updateEmail_shouldCallUserService_whenRequestSucceeds() {

        // given
        UUID userId = UUID.randomUUID();
        String email = "new@gmail.com";

        when(jwtGenerator.generateServiceToken(
                "auth-service",
                "user-service",
                "user.internal.email.update"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                eq("http://USER/api/v1/internal/users/{id}/email"),
                eq(org.springframework.http.HttpMethod.PATCH),
                any(),
                eq(UserResponse.class),
                eq(userId)
        )).thenReturn(ResponseEntity.ok().build());

        // when
        userClient.updateEmail(userId, email);

        // then
        verify(restTemplate)
                .exchange(
                        eq("http://USER/api/v1/internal/users/{id}/email"),
                        eq(org.springframework.http.HttpMethod.PATCH),
                        any(),
                        eq(UserResponse.class),
                        eq(userId)
                );
    }

    @Test
    void updateEmail_shouldThrowEntityNotFound_whenUserDoesNotExist() {

        // given
        UUID userId = UUID.randomUUID();
        String email = "new@gmail.com";

        when(jwtGenerator.generateServiceToken(
                "auth-service",
                "user-service",
                "user.internal.email.update"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                anyString(),
                eq(org.springframework.http.HttpMethod.PATCH),
                any(),
                eq(UserResponse.class),
                eq(userId)
        )).thenThrow(
                HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND,
                        "Not Found",
                        null,
                        null,
                        null
                )
        );

        // when & then
        assertThatThrownBy(() ->
                userClient.updateEmail(userId, email)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found: " + userId);
    }

    @Test
    void getUsersByIds_shouldReturnUsers_whenRequestSucceeds() {

        // given
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        UserResponse firstUser =
                new UserResponse(firstId, "Radwan Rahmoun");

        UserResponse secondUser =
                new UserResponse(secondId, "John Doe");

        when(jwtGenerator.generateServiceToken(
                "auth-service",
                "user-service",
                "user.internal.users-by-ids.read"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                eq("http://USER/api/v1/internal/users/by-ids"),
                eq(org.springframework.http.HttpMethod.POST),
                any(),
                eq(UserResponse[].class)
        )).thenReturn(
                ResponseEntity.ok(
                        new UserResponse[]{
                                firstUser,
                                secondUser
                        }
                )
        );

        // when
        List<UserResponse> result =
                userClient.getUsersByIds(
                        List.of(firstId, secondId)
                );

        // then
        assertThat(result)
                .containsExactly(
                        firstUser,
                        secondUser
                );
    }

    @Test
    void getUsersByIds_shouldReturnEmptyList_whenResponseBodyIsNull() {

        // given
        when(jwtGenerator.generateServiceToken(
                "auth-service",
                "user-service",
                "user.internal.users-by-ids.read"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                eq("http://USER/api/v1/internal/users/by-ids"),
                eq(org.springframework.http.HttpMethod.POST),
                any(),
                eq(UserResponse[].class)
        )).thenReturn(
                ResponseEntity.ok(null)
        );

        // when
        List<UserResponse> result =
                userClient.getUsersByIds(List.of(UUID.randomUUID()));

        // then
        assertThat(result)
                .isEmpty();
    }

    @Test
    void searchUsersByName_shouldReturnUsers_whenRequestSucceeds() {

        // given
        String search = "Radwan";

        UserResponse user =
                new UserResponse(
                        UUID.randomUUID(),
                        "Radwan Rahmoun"
                );

        when(jwtGenerator.generateServiceToken(
                "auth-service",
                "user-service",
                "user.internal.search-by-fullName.read"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                eq("http://USER/api/v1/internal/users/search?fullName={fullName}"),
                eq(org.springframework.http.HttpMethod.GET),
                any(),
                eq(UserResponse[].class),
                eq(search)
        )).thenReturn(
                ResponseEntity.ok(
                        new UserResponse[]{user}
                )
        );

        // when
        List<UserResponse> result =
                userClient.searchUsersByName(search);

        // then
        assertThat(result)
                .containsExactly(user);
    }

    @Test
    void searchUsersByName_shouldReturnEmptyList_whenResponseBodyIsNull() {

        // given
        when(jwtGenerator.generateServiceToken(
                "auth-service",
                "user-service",
                "user.internal.search-by-fullName.read"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                eq("http://USER/api/v1/internal/users/search?fullName={fullName}"),
                eq(org.springframework.http.HttpMethod.GET),
                any(),
                eq(UserResponse[].class),
                anyString()
        )).thenReturn(
                ResponseEntity.ok(null)
        );

        // when
        List<UserResponse> result =
                userClient.searchUsersByName("Radwan");

        // then
        assertThat(result)
                .isEmpty();
    }

    @Test
    void searchUsersByName_shouldThrowEntityNotFound_whenUsersDoNotExist() {

        // given
        String search = "Unknown";

        when(jwtGenerator.generateServiceToken(
                "auth-service",
                "user-service",
                "user.internal.search-by-fullName.read"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                anyString(),
                eq(org.springframework.http.HttpMethod.GET),
                any(),
                eq(UserResponse[].class),
                eq(search)
        )).thenThrow(
                HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND,
                        "Not Found",
                        null,
                        null,
                        null
                )
        );

        // when & then
        assertThatThrownBy(() ->
                userClient.searchUsersByName(search)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Users not found");
    }
}
