package com.example.smart_fuel_management_system.service.impl.client;

import com.example.smart_fuel_management_system.dto.StationFuelSessionResponse;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.exception.ServiceUnavailableException;
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

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class StationClient {

    private static final String BASE_URL =
            "http://STATION/api/v1/internal/stations";

    private final RestTemplate restTemplate;
    private final JwtService jwtService;

    public Optional<BigDecimal> getPrice(UUID stationId, FuelType fuelType) {

        HttpHeaders headers = generateServiceToken("station.internal.price.read");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {

            ResponseEntity<BigDecimal> response =
                    restTemplate.exchange(
                            BASE_URL + "/{stationId}/fuel-prices/{fuelType}",
                            HttpMethod.GET,
                            entity,
                            BigDecimal.class,
                            stationId,
                            fuelType
                    );

            return Optional.ofNullable(response.getBody());

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("station not found: {}", stationId);
            return Optional.empty();
        } catch (RestClientException e) {
            log.warn("station service unavailable: {}", stationId, e);
            return Optional.empty();
        } catch (ServiceUnavailableException e) {
            log.warn("No STATION instance available: {}", stationId);
            return Optional.empty();
        }
    }

    public Optional<StationFuelSessionResponse> getStationIdAndPumpId(UUID pumpId) {

        HttpHeaders headers = generateServiceToken("station.internal.station-id.read");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try{
            ResponseEntity<StationFuelSessionResponse> response =
                    restTemplate.exchange(
                            "http://STATION/api/v1/internal/stations/pumps/{pumpId}",
                            HttpMethod.GET,
                            entity,
                            StationFuelSessionResponse.class,
                            pumpId
                    );

            return Optional.ofNullable(response.getBody());

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("station not found: {}", pumpId);
            return Optional.empty();
        } catch (RestClientException e) {
            log.warn("station service unavailable: {}", pumpId, e);
            return Optional.empty();
        } catch (ServiceUnavailableException e) {
            log.warn("No STATION instance available: {}", pumpId);
            return Optional.empty();
        }
    }

    private HttpHeaders generateServiceToken(String scope) {

        String serviceToken = jwtService.generateServiceToken(
                "fuel-session-service",
                "station-service",
            scope
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceToken);

        return headers;
    }
}