package com.example.smart_fuel_management_system.service.impl.client;

import com.example.smart_fuel_management_system.dto.station.StationEmployeeResponse;
import com.example.smart_fuel_management_system.exceptions.ServiceUnavailableException;
import com.example.smart_fuel_management_system.security.JwtService;
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
public class AuthClient {

    private static final String BASE_URL = "http://AUTH/api/v1/internal/auth";
    private final RestTemplate restTemplate;
    private final JwtService jwtService;

    public Optional<StationEmployeeResponse> getEmployeeInfo(UUID employeeId){
        HttpEntity<Void> entity =
                new HttpEntity<>(generateServiceToken());

        try {
            ResponseEntity<StationEmployeeResponse> response =
                    restTemplate.exchange(
                            BASE_URL + "/stations/" + employeeId + "/employee",
                            HttpMethod.GET,
                            entity,
                            StationEmployeeResponse.class
                    );

            return Optional.ofNullable(response.getBody());
        }  catch (HttpClientErrorException.NotFound e) {
            log.warn("auth not found: {}", employeeId);
            return Optional.empty();
        } catch (RestClientException e) {
            log.warn("auth service unavailable: {}", employeeId, e);
            return Optional.empty();
        } catch (ServiceUnavailableException e) {
            log.warn("No AUTH instance available: {}", employeeId);
            return Optional.empty();
        }
    }

    private HttpHeaders generateServiceToken(){
        String serviceToken = jwtService.generateServiceToken(
                "station-service",
                "auth-service",
                "auth.internal.station-employee.get"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceToken);

        return headers;
    }
}
