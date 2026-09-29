package com.example.smart_fuel_management_system.service.impl.auth.client;

import com.example.smart_fuel_management_system.dto.UserResponse;
import com.example.smart_fuel_management_system.exceptions.ServiceUnavailableException;
import com.example.smart_fuel_management_system.security.JWTGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserClient {

    private final RestTemplate restTemplate;
    private final JWTGenerator jwtGenerator;

    public UserResponse getUserById(UUID userId) {

        HttpEntity<Void> entity =
                new HttpEntity<>(generateServiceToken("user.internal.user.read"));

        try {
            ResponseEntity<UserResponse> response =
                    restTemplate.exchange(
                            "http://USER/api/v1/internal/users/{id}",
                            HttpMethod.GET,
                            entity,
                            UserResponse.class,
                            userId
                    );

            return response.getBody();

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("user data not found for user: {}", userId);
            throw new EntityNotFoundException(
                    "User not found: " + userId
            );
        } catch (RestClientException e) {
            throw new ServiceUnavailableException(
                    "User service is unavailable",
                    e
            );
        } catch (ServiceUnavailableException e) {
            throw new ServiceUnavailableException("No USER instance available", e);
        }
    }

    public void updateEmail(UUID userId, String email){

        HttpEntity<String> entity =
                new HttpEntity<>(email, generateServiceToken("user.internal.email.update"));

        try {
            restTemplate.exchange(
                    "http://USER/api/v1/internal/users/{id}/email",
                    HttpMethod.PATCH,
                    entity,
                    UserResponse.class,
                    userId
            );

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("user data not found for user: {}", userId);
            throw new EntityNotFoundException(
                    "User not found: " + userId
            );
        } catch (RestClientException e) {
            throw new ServiceUnavailableException(
                    "User service is unavailable",
                    e
            );
        } catch (ServiceUnavailableException e) {
            throw new ServiceUnavailableException("No USER instance available", e);
        }
    }

    public List<UserResponse> getUsersByIds(List<UUID> userIds) {
        try {
            HttpEntity<List<UUID>> entity =
                    new HttpEntity<>(
                            userIds,
                            generateServiceToken("user.internal.users-by-ids.read")
                    );

            ResponseEntity<UserResponse[]> response =
                    restTemplate.exchange(
                            "http://USER/api/v1/internal/users/by-ids",
                            HttpMethod.POST,
                            entity,
                            UserResponse[].class
                    );

            return response.getBody() == null
                    ? List.of()
                    : Arrays.asList(response.getBody());

        } catch (HttpClientErrorException.NotFound e) {

            throw new EntityNotFoundException(
                    "Users not found"
            );

        } catch (RestClientException e) {

            throw new ServiceUnavailableException(
                    "User service is unavailable",
                    e
            );
        }
    }

    public List<UserResponse> searchUsersByName(String search) {
        try {
            HttpEntity<Void> entity =
                    new HttpEntity<>(
                            generateServiceToken("user.internal.search-by-fullName.read")
                    );

            ResponseEntity<UserResponse[]> response =
                    restTemplate.exchange(
                            "http://USER/api/v1/internal/users/search?fullName={fullName}",
                            HttpMethod.GET,
                            entity,
                            UserResponse[].class,
                            search
                    );

            return response.getBody() == null
                    ? List.of()
                    : Arrays.asList(response.getBody());

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("erroe:",e);
            throw new EntityNotFoundException(
                    "Users not found"
            );

        } catch (RestClientException e) {

            throw new ServiceUnavailableException(
                    "User service is unavailable",
                    e
            );
        }
    }

    private HttpHeaders generateServiceToken(String scope){
        String serviceToken = jwtGenerator.generateServiceToken(
                "auth-service",
                "user-service",
                scope
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceToken);

        return headers;
    }


}