package com.example.smart_fuel_management_system.service.impl.client;

import com.example.smart_fuel_management_system.dto.AuthUserResponse;
import com.example.smart_fuel_management_system.enums.AccountStatusType;
import com.example.smart_fuel_management_system.enums.RoleType;
import com.example.smart_fuel_management_system.exceptions.ServiceUnavailableException;
import com.example.smart_fuel_management_system.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthClient {

    private final RestTemplate restTemplate;
    private final JwtService jwtService;

    public List<AuthUserResponse> getUsers(List<UUID> userIds) {
        HttpEntity<List<UUID>> entity = new HttpEntity<>
                (userIds, generateServiceToken("auth.internal.info.read"));

        try {
            ResponseEntity<List<AuthUserResponse>> response =
                    restTemplate.exchange(
                            "http://AUTH/api/v1/internal/auth/by-ids",
                            HttpMethod.POST,
                            entity,
                            new ParameterizedTypeReference<List<AuthUserResponse>>() {}
                    );

            return response.getBody();

        } catch (RestClientException e) {
            throw new ServiceUnavailableException(
                    "auth service is unavailable",
                    e
            );
        } catch (ServiceUnavailableException e) {
            throw new ServiceUnavailableException("No AUTH instance available", e);
        }
    }

    public List<AuthUserResponse> searchByPhone(
            String search,
            Pageable pageable) {

        HttpEntity<Void> entity =
                new HttpEntity<>(generateServiceToken("auth.internal.search.read"));

        try {
            ResponseEntity<List<AuthUserResponse>> response =
                    restTemplate.exchange(
                            "http://AUTH/api/v1/internal/auth/search/phone" +
                                    "?search={search}&page={page}&size={size}",
                            HttpMethod.GET,
                            entity,
                            new ParameterizedTypeReference<List<AuthUserResponse>>() {},
                            search,
                            pageable.getPageNumber(),
                            pageable.getPageSize()
                    );

            return response.getBody();

        } catch (RestClientException e) {
            throw new ServiceUnavailableException(
                    "Auth service is unavailable",
                    e
            );
        } catch (ServiceUnavailableException e) {
            throw new ServiceUnavailableException("No AUTH instance available", e);
        }
    }

    public List<AuthUserResponse> getUsersByStatus(
            AccountStatusType status,
            Pageable pageable) {

        HttpEntity<Void> entity =
                new HttpEntity<>(generateServiceToken("auth.internal.search.read"));

        try {
            ResponseEntity<List<AuthUserResponse>> response =
                    restTemplate.exchange(
                            "http://AUTH/api/v1/internal/auth/filter/status" +
                                    "?status={status}&page={page}&size={size}",
                            HttpMethod.GET,
                            entity,
                            new ParameterizedTypeReference<List<AuthUserResponse>>() {},
                            status,
                            pageable.getPageNumber(),
                            pageable.getPageSize()
                    );

            return response.getBody();

        } catch (RestClientException e) {
            throw new ServiceUnavailableException(
                    "Auth service is unavailable",
                    e
            );
        } catch (ServiceUnavailableException e) {
            throw new ServiceUnavailableException("No AUTH instance available", e);
        }
    }

    private HttpHeaders generateServiceToken(String scope){
        String serviceToken = jwtService.generateServiceToken(
                "user-service",
                "auth-service",
                scope
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceToken);

        return headers;
    }


}
