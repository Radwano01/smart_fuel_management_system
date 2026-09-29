package com.example.smart_fuel_management_system.service.client;

import com.example.smart_fuel_management_system.dto.TransactionCountResponse;
import com.example.smart_fuel_management_system.dto.VehicleCountResponse;
import com.example.smart_fuel_management_system.dto.VehicleDashboardSummaryResponse;
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
public class VehicleClient {

    private final RestTemplate restTemplate;

    private final JWTService jwtGenerator;

    public Optional<VehicleDashboardSummaryResponse> getVehicleSummary() {

        try {
            HttpEntity<Void> entity =
                    new HttpEntity<>(generateServiceToken("vehicle.internal.dashboard-summary.read"));

            ResponseEntity<VehicleDashboardSummaryResponse> response =
                    restTemplate.exchange(
                            "http://VEHICLE/api/v1/internal/vehicles/dashboard/summary",
                            HttpMethod.GET,
                            entity,
                            VehicleDashboardSummaryResponse.class
                    );

            return Optional.ofNullable(response.getBody());

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Vehicle data not found for dashboard");
            return Optional.empty();

        } catch (RestClientException e) {
            // Service down, timeout, connection failure, etc.
            log.warn("Vehicle service unavailable for dashboard", e);
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            log.warn(
                    "No VEHICLE instance available"
            );
            return Optional.empty();
        }
    }

    public Optional<VehicleCountResponse> getUserVehicleCount(UUID userId){
        try {
            HttpEntity<Void> entity =
                    new HttpEntity<>(generateServiceToken("vehicle.internal.vehicle-count.read"));

            ResponseEntity<VehicleCountResponse> response =
                    restTemplate.exchange(
                            "http://VEHICLE/api/v1/internal/vehicles/users/{userId}",
                            HttpMethod.GET,
                            entity,
                            VehicleCountResponse.class,
                            userId
                    );

            return Optional.ofNullable(response.getBody());

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Vehicle data not found for dashboard");
            return Optional.empty();

        } catch (RestClientException e) {
            // Service down, timeout, connection failure, etc.
            log.warn("Vehicle service unavailable for dashboard", e);
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            log.warn(
                    "No VEHICLE instance available"
            );
            return Optional.empty();
        }
    }

    private HttpHeaders generateServiceToken(String scope){
        String serviceToken = jwtGenerator.generateServiceToken(
                "dashboard-service",
                "vehicle-service",
                scope
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceToken);

        return headers;
    }
}