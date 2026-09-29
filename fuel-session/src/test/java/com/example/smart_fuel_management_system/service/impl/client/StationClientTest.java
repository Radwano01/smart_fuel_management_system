package com.example.smart_fuel_management_system.service.impl.client;

import com.example.smart_fuel_management_system.dto.StationFuelSessionResponse;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.exception.ServiceUnavailableException;
import com.example.smart_fuel_management_system.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JwtService jwtService;

    @Mock
    private ResponseEntity<BigDecimal> priceResponse;

    @Mock
    private ResponseEntity<StationFuelSessionResponse> stationResponse;

    @InjectMocks
    private StationClient stationClient;

    @Test
    void getPrice_shouldReturnPrice_whenPriceIsFound() {
        // given
        UUID stationId = UUID.randomUUID();
        FuelType fuelType = FuelType.GASOLINE;
        BigDecimal price = new BigDecimal("40.50");

        when(jwtService.generateServiceToken(
                "fuel-session-service",
                "station-service",
                "station.internal.price.read"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BigDecimal.class),
                eq(stationId),
                eq(fuelType)
        )).thenReturn(priceResponse);

        when(priceResponse.getBody())
                .thenReturn(price);

        // when
        Optional<BigDecimal> result =
                stationClient.getPrice(
                        stationId,
                        fuelType
                );

        // then
        assertThat(result)
                .isPresent()
                .contains(price);
    }

    @Test
    void getPrice_shouldReturnEmpty_whenPriceIsNotFound() {
        // given
        UUID stationId = UUID.randomUUID();
        FuelType fuelType = FuelType.GASOLINE;

        when(jwtService.generateServiceToken(
                "fuel-session-service",
                "station-service",
                "station.internal.price.read"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BigDecimal.class),
                eq(stationId),
                eq(fuelType)
        )).thenThrow(
                HttpClientErrorException.NotFound.class
        );

        // when
        Optional<BigDecimal> result =
                stationClient.getPrice(
                        stationId,
                        fuelType
                );

        // then
        assertThat(result)
                .isEmpty();
    }

    @Test
    void getPrice_shouldReturnEmpty_whenStationServiceThrowsException() {
        // given
        UUID stationId = UUID.randomUUID();
        FuelType fuelType = FuelType.GASOLINE;

        when(jwtService.generateServiceToken(
                "fuel-session-service",
                "station-service",
                "station.internal.price.read"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BigDecimal.class),
                eq(stationId),
                eq(fuelType)
        )).thenThrow(
                new RestClientException(
                        "Station service unavailable"
                )
        );

        // when
        Optional<BigDecimal> result =
                stationClient.getPrice(
                        stationId,
                        fuelType
                );

        // then
        assertThat(result)
                .isEmpty();
    }

    @Test
    void getPrice_shouldReturnEmpty_whenNoStationInstanceIsAvailable() {
        // given
        UUID stationId = UUID.randomUUID();
        FuelType fuelType = FuelType.GASOLINE;

        when(jwtService.generateServiceToken(
                "fuel-session-service",
                "station-service",
                "station.internal.price.read"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BigDecimal.class),
                eq(stationId),
                eq(fuelType)
        )).thenThrow(
                new ServiceUnavailableException(
                        "No STATION instance available",
                        new RuntimeException("Station service unavailable")
                )
        );

        // when
        Optional<BigDecimal> result =
                stationClient.getPrice(
                        stationId,
                        fuelType
                );

        // then
        assertThat(result)
                .isEmpty();
    }

    @Test
    void getStationIdAndPumpId_shouldReturnStation_whenPumpIsFound() {
        // given
        UUID pumpId = UUID.randomUUID();

        StationFuelSessionResponse station =
                org.mockito.Mockito.mock(
                        StationFuelSessionResponse.class
                );

        when(jwtService.generateServiceToken(
                "fuel-session-service",
                "station-service",
                "station.internal.station-id.read"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(StationFuelSessionResponse.class),
                eq(pumpId)
        )).thenReturn(stationResponse);

        when(stationResponse.getBody())
                .thenReturn(station);

        // when
        Optional<StationFuelSessionResponse> result =
                stationClient.getStationIdAndPumpId(pumpId);

        // then
        assertThat(result)
                .isPresent()
                .containsSame(station);
    }

    @Test
    void getStationIdAndPumpId_shouldReturnEmpty_whenPumpIsNotFound() {
        // given
        UUID pumpId = UUID.randomUUID();

        when(jwtService.generateServiceToken(
                "fuel-session-service",
                "station-service",
                "station.internal.station-id.read"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(StationFuelSessionResponse.class),
                eq(pumpId)
        )).thenThrow(
                HttpClientErrorException.NotFound.class
        );

        // when
        Optional<StationFuelSessionResponse> result =
                stationClient.getStationIdAndPumpId(pumpId);

        // then
        assertThat(result)
                .isEmpty();
    }

    @Test
    void getStationIdAndPumpId_shouldReturnEmpty_whenStationServiceThrowsException() {
        // given
        UUID pumpId = UUID.randomUUID();

        when(jwtService.generateServiceToken(
                "fuel-session-service",
                "station-service",
                "station.internal.station-id.read"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(StationFuelSessionResponse.class),
                eq(pumpId)
        )).thenThrow(
                new RestClientException(
                        "Station service unavailable"
                )
        );

        // when
        Optional<StationFuelSessionResponse> result =
                stationClient.getStationIdAndPumpId(pumpId);

        // then
        assertThat(result)
                .isEmpty();
    }

    @Test
    void getStationIdAndPumpId_shouldReturnEmpty_whenNoStationInstanceIsAvailable() {
        // given
        UUID pumpId = UUID.randomUUID();

        when(jwtService.generateServiceToken(
                "fuel-session-service",
                "station-service",
                "station.internal.station-id.read"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(StationFuelSessionResponse.class),
                eq(pumpId)
        )).thenThrow(
                new ServiceUnavailableException(
                        "No STATION instance available",
                        new RuntimeException("Station service unavailable")
                )
        );

        // when
        Optional<StationFuelSessionResponse> result =
                stationClient.getStationIdAndPumpId(pumpId);

        // then
        assertThat(result)
                .isEmpty();
    }
}
