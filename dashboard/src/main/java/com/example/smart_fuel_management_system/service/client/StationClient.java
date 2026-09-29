package com.example.smart_fuel_management_system.service.client;

import com.example.smart_fuel_management_system.dto.StationDashboardSummaryResponse;
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

@Slf4j
@RequiredArgsConstructor
@Component
public class StationClient {

    private final RestTemplate restTemplate;
    private final JWTService jwtGenerator;

    public Optional<StationDashboardSummaryResponse> getStationSummary() {

        try {
            HttpEntity<Void> entity =
                    new HttpEntity<>(generateServiceToken());

            ResponseEntity<StationDashboardSummaryResponse> response =
                    restTemplate.exchange(
                            "http://STATION/api/v1/internal/stations/dashboard/summary",
                            HttpMethod.GET,
                            entity,
                            StationDashboardSummaryResponse.class
                    );

            return Optional.ofNullable(response.getBody());

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Station data not found");
            return Optional.empty();

        } catch (RestClientException e) {
            log.warn(
                    "Station service unavailable for dashboard: ",
                    e
            );
            return Optional.empty();

        } catch (IllegalArgumentException e) {
            log.warn(
                    "No STATION instance available"
            );
            return Optional.empty();
        }
    }


    private HttpHeaders generateServiceToken(){
        String serviceToken = jwtGenerator.generateServiceToken(
                "dashboard-service",
                "station-service",
                "station.internal.station-summary.read"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceToken);

        return headers;
    }
}
