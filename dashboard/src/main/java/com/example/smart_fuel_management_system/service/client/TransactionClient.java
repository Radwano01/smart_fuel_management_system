package com.example.smart_fuel_management_system.service.client;

import com.example.smart_fuel_management_system.dto.TransactionCountResponse;
import com.example.smart_fuel_management_system.dto.TransactionDashboardSummaryResponse;
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
public class TransactionClient {

    private final RestTemplate restTemplate;
    private final JWTService jwtGenerator;

    public Optional<TransactionDashboardSummaryResponse> getDashboardSummary() {

        try {
            HttpEntity<Void> entity =
                    new HttpEntity<>(generateServiceToken("transaction.internal.user-count.read"));

            ResponseEntity<TransactionDashboardSummaryResponse> response =
                    restTemplate.exchange(
                            "http://TRANSACTION/api/v1/internal/transactions/dashboard/summary",
                            HttpMethod.GET,
                            entity,
                            TransactionDashboardSummaryResponse.class
                    );

            return Optional.ofNullable(response.getBody());

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Transaction data not found for user");
            return Optional.empty();

        } catch (RestClientException e) {
            log.warn(
                    "Transaction service unavailable for user",
                    e
            );
            return Optional.empty();

        } catch (IllegalArgumentException e) {
            log.warn(
                    "No TRANSACTION instance available"
            );
            return Optional.empty();
        }
    }

    public Optional<TransactionCountResponse> getUserTransactionsCount(UUID userId){

        try {
            HttpEntity<Void> entity =
                    new HttpEntity<>(generateServiceToken("transaction.internal.dashboard-summary.read"));

            ResponseEntity<TransactionCountResponse> response =
                    restTemplate.exchange(
                            "http://TRANSACTION/api/v1/internal/transactions/users/{userId}",
                            HttpMethod.GET,
                            entity,
                            TransactionCountResponse.class,
                            userId
                    );

            return Optional.ofNullable(response.getBody());

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Transaction data not found for user");
            return Optional.empty();

        } catch (RestClientException e) {
            log.warn(
                    "Transaction service unavailable for user",
                    e
            );
            return Optional.empty();

        } catch (IllegalArgumentException e) {
            log.warn(
                    "No TRANSACTION instance available"
            );
            return Optional.empty();
        }
    }

    private HttpHeaders generateServiceToken(String scope){
        String serviceToken = jwtGenerator.generateServiceToken(
                "dashboard-service",
                "transaction-service",
                scope
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceToken);

        return headers;
    }
}