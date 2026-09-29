package com.example.smart_fuel_management_system.service.impl.client;

import com.example.smart_fuel_management_system.dto.station.TransactionStationResponse;
import com.example.smart_fuel_management_system.exceptions.ServiceUnavailableException;
import com.example.smart_fuel_management_system.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionClient {

    private static final String BASE_URL = "http://TRANSACTION/api/v1/internal/transactions";

    private final RestTemplate restTemplate;

    private final JwtService jwtService;

    public Optional<TransactionStationResponse> getTransactionsAndVehiclesCount(UUID stationId){
        HttpEntity<Void> entity =
                new HttpEntity<>(generateServiceToken());

        try {
            ResponseEntity<TransactionStationResponse> response =
                    restTemplate.exchange(
                            BASE_URL + "/stations/" + stationId,
                            HttpMethod.GET,
                            entity,
                            TransactionStationResponse.class
                    );

            return Optional.ofNullable(response.getBody());
        }  catch (HttpClientErrorException.NotFound e) {
            log.warn("transaction not found: {}", stationId);
            return Optional.empty();

        } catch (RestClientException e) {
            log.warn("transaction service unavailable: {}", stationId, e);
            return Optional.empty();
        } catch (ServiceUnavailableException e) {
            log.warn("No TRANSACTION instance available: {}", stationId);
            return Optional.empty();
        }
    }

    public List<TransactionStationResponse> getTransactionsAndVehiclesCount(List<UUID> ids) {

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        try {
            HttpEntity<List<UUID>> entity =
                    new HttpEntity<>(
                            ids,
                            generateServiceToken()
                    );

            ResponseEntity<List<TransactionStationResponse>> response =
                    restTemplate.exchange(
                            BASE_URL + "/stations/by-ids",
                            HttpMethod.POST,
                            entity,
                            new ParameterizedTypeReference<>() {}
                    );

            return response.getBody() != null
                    ? response.getBody()
                    : List.of();

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Transaction statistics not found for stations: {}", ids);
            return List.of();

        } catch (RestClientException e) {
            log.warn("Transaction service unavailable: {}", ids, e);
            return List.of();

        } catch (IllegalArgumentException e) {
            log.warn("No TRANSACTION instance available");
            return List.of();
        }
    }

    private HttpHeaders generateServiceToken(){
        String serviceToken = jwtService.generateServiceToken(
                "station-service",
                "transaction-service",
                "user.internal.station-count.get"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceToken);

        return headers;
    }
}
