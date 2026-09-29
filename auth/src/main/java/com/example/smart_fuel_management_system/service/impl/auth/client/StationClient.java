package com.example.smart_fuel_management_system.service.impl.auth.client;

import com.example.smart_fuel_management_system.dto.StationEmployeeAuthResponse;
import com.example.smart_fuel_management_system.exceptions.ServiceUnavailableException;
import com.example.smart_fuel_management_system.security.JWTGenerator;
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

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class StationClient {

    private final RestTemplate restTemplate;
    private final JWTGenerator jwtGenerator;


    public Optional<StationEmployeeAuthResponse> getStationDetails(UUID employeeId) {

        HttpEntity<Void> entity =
                new HttpEntity<>(generateServiceToken("station.internal.station-details.read"));

        try {
            ResponseEntity<StationEmployeeAuthResponse> response =
                    restTemplate.exchange(
                            "http://STATION/api/v1/internal/station-employees/{id}",
                            HttpMethod.GET,
                            entity,
                            StationEmployeeAuthResponse.class,
                            employeeId
                    );

            return Optional.ofNullable(response.getBody());

        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw new ServiceUnavailableException(
                    "Station service is unavailable",
                    e
            );
        } catch (ServiceUnavailableException e) {
            throw new ServiceUnavailableException("No STATION instance available", e);
        }
    }


    private HttpHeaders generateServiceToken(String scope){
        String serviceToken = jwtGenerator.generateServiceToken(
                "auth-service",
                "station-service",
                scope
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceToken);

        return headers;
    }
}
