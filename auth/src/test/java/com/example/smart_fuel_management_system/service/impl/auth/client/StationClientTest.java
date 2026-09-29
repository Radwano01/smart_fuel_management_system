package com.example.smart_fuel_management_system.service.impl.auth.client;

import com.example.smart_fuel_management_system.dto.StationEmployeeAuthResponse;
import com.example.smart_fuel_management_system.exceptions.ServiceUnavailableException;
import com.example.smart_fuel_management_system.security.JWTGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StationClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JWTGenerator jwtGenerator;

    @InjectMocks
    private StationClient stationClient;

    @Test
    void getStationDetails_shouldReturnStation_whenRequestSucceeds() {

        // given
        UUID employeeId = UUID.randomUUID();

        StationEmployeeAuthResponse station =
                mock(StationEmployeeAuthResponse.class);

        when(jwtGenerator.generateServiceToken(
                "auth-service",
                "station-service",
                "station.internal.station-details.read"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                eq("http://STATION/api/v1/internal/station-employees/{id}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(StationEmployeeAuthResponse.class),
                eq(employeeId)
        )).thenReturn(ResponseEntity.ok(station));

        // when
        Optional<StationEmployeeAuthResponse> result =
                stationClient.getStationDetails(employeeId);

        // then
        assertThat(result)
                .isPresent()
                .contains(station);
    }

    @Test
    void getStationDetails_shouldReturnEmpty_whenStationNotFound() {

        // given
        UUID employeeId = UUID.randomUUID();

        when(jwtGenerator.generateServiceToken(
                "auth-service",
                "station-service",
                "station.internal.station-details.read"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(StationEmployeeAuthResponse.class),
                eq(employeeId)
        )).thenThrow(
                HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND,
                        "Not Found",
                        null,
                        null,
                        null
                )
        );

        // when
        Optional<StationEmployeeAuthResponse> result =
                stationClient.getStationDetails(employeeId);

        // then
        assertThat(result)
                .isEmpty();
    }

    @Test
    void getStationDetails_shouldThrowServiceUnavailable_whenRequestFails() {

        // given
        UUID employeeId = UUID.randomUUID();

        when(jwtGenerator.generateServiceToken(
                "auth-service",
                "station-service",
                "station.internal.station-details.read"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(StationEmployeeAuthResponse.class),
                eq(employeeId)
        )).thenThrow(
                new RestClientException("Connection failed")
        );

        // when & then
        assertThatThrownBy(() ->
                stationClient.getStationDetails(employeeId)
        )
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessage("Station service is unavailable");
    }

    @Test
    void getStationDetails_shouldThrowServiceUnavailable_whenNoStationInstanceAvailable() {

        // given
        UUID employeeId = UUID.randomUUID();

        when(jwtGenerator.generateServiceToken(
                "auth-service",
                "station-service",
                "station.internal.station-details.read"
        )).thenThrow(
                new ServiceUnavailableException(
                        "No STATION instance available",
                        new RuntimeException()
                )
        );
        // when & then
        assertThatThrownBy(() ->
                stationClient.getStationDetails(employeeId)
        )
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessage("No STATION instance available");
    }
}
