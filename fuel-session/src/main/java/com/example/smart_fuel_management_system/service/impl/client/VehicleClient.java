package com.example.smart_fuel_management_system.service.impl.client;

import com.example.smart_fuel_management_system.dto.CaptureRequest;
import com.example.smart_fuel_management_system.dto.VehicleResponseDTO;
import com.example.smart_fuel_management_system.exception.ServiceUnavailableException;
import com.example.smart_fuel_management_system.exception.StationServiceUnavailableException;
import com.example.smart_fuel_management_system.security.JwtService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class VehicleClient {

    private final RestTemplate restTemplate;
    private final JwtService jwtService;

    public Optional<VehicleResponseDTO> resolveVehicle(String rfidTag, String plateNumber) {

        HttpHeaders headers = generateServiceToken();

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            return Optional.ofNullable(restTemplate.exchange(
                    "http://VEHICLE/api/v1/internal/vehicles/resolve?rfidTag={rfidTag}&plateNumber={plateNumber}",
                    HttpMethod.GET,
                    entity,
                    VehicleResponseDTO.class,
                    rfidTag,
                    plateNumber
            ).getBody());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("vehicle not found: {}", "plate number: " + plateNumber);
            return Optional.empty();
        } catch (RestClientException e) {
            log.warn("vehicle service unavailable: {}", plateNumber, e);
            return Optional.empty();
        } catch (ServiceUnavailableException e) {
            log.warn("No VEHICLE instance available: {}", plateNumber);
            return Optional.empty();
        }
    }

    private HttpHeaders generateServiceToken(){
        String serviceToken = jwtService.generateServiceToken(
                "fuel-session-service",
                "vehicle-service",
                "vehicle.internal.resolve"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceToken);

        return headers;
    }
}