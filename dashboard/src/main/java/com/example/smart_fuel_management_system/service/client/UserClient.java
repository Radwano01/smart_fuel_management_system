package com.example.smart_fuel_management_system.service.client;

import com.example.smart_fuel_management_system.dto.TransactionDashboardSummaryResponse;
import com.example.smart_fuel_management_system.dto.UserDashboardSummaryResponse;
import com.example.smart_fuel_management_system.security.JWTService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserClient {

    private final RestTemplate restTemplate;
    private final JWTService jwtGenerator;

    public Optional<UserDashboardSummaryResponse> getUserSummary() {

        try {
            HttpEntity<Void> entity =
                    new HttpEntity<>(generateServiceToken());

            ResponseEntity<UserDashboardSummaryResponse> response =
                    restTemplate.exchange(
                            "http://USER/api/v1/internal/users/dashboard/summary",
                            HttpMethod.GET,
                            entity,
                            UserDashboardSummaryResponse.class
                    );

            return Optional.ofNullable(response.getBody());

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("User data not found for dashboard");
            return Optional.empty();

        } catch (RestClientException e) {
            log.warn(
                    "User service unavailable for dashboard: ",
                    e
            );
            return Optional.empty();

        } catch (IllegalArgumentException e) {
            log.warn(
                    "No USER instance available"
            );
            return Optional.empty();
        }
    }


    private HttpHeaders generateServiceToken(){
        String serviceToken = jwtGenerator.generateServiceToken(
                "dashboard-service",
                "user-service",
                "user.internal.dashboard-summary.read"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceToken);

        return headers;
    }
}
