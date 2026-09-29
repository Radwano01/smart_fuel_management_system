package com.example.smart_fuel_management_system.service.impl.client;


import com.example.smart_fuel_management_system.dto.VehicleTransactionResponse;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@Component
public class VehicleClient {

    private static final  String BASE_URL = "/api/v1/internal/vehicles";

    private final RestTemplate restTemplate;
    private final JwtService jwtService;

    public Optional<VehicleTransactionResponse> getVehicleDetails(UUID id) {
        try {
            HttpEntity<Void> entity =
                    new HttpEntity<>(generateServiceToken());

            ResponseEntity<VehicleTransactionResponse> response =
                    restTemplate.exchange(
                            "http://VEHICLE" + BASE_URL + "/{id}",
                            HttpMethod.GET,
                            entity,
                            VehicleTransactionResponse.class,
                            id
                    );

            return Optional.ofNullable(response.getBody());

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Vehicle not found: {}", id);
            return Optional.empty();

        } catch (RestClientException e) {
            log.warn("Vehicle service unavailable: {}", id, e);
            return Optional.empty();
        } catch (ServiceUnavailableException e) {
            log.warn("No VEHICLE instance available: {}", id);
            return Optional.empty();
        }
    }

    public List<VehicleTransactionResponse> getVehiclesDetails(List<UUID> ids) {

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        try {
            HttpEntity<List<UUID>> entity =
                    new HttpEntity<>(
                            ids,
                            generateServiceToken()
                    );

            ResponseEntity<List<VehicleTransactionResponse>> response =
                    restTemplate.exchange(
                            "http://VEHICLE" + BASE_URL + "/by-ids",
                            HttpMethod.POST,
                            entity,
                            new ParameterizedTypeReference<>() {}
                    );

            return response.getBody() != null
                    ? response.getBody()
                    : List.of();

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Vehicles not found: {}", ids);
            return List.of();

        } catch (RestClientException e) {
            log.warn("Vehicle service unavailable: {}", ids, e);
            return List.of();

        } catch (ServiceUnavailableException e) {
            log.warn("No VEHICLE instance available");
            return List.of();
        }
    }

    private HttpHeaders generateServiceToken(){
        String serviceToken = jwtService.generateServiceToken(
                "transaction-service",
                "vehicle-service",
                "vehicle.internal.read"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceToken);

        return headers;
    }
}
