package com.example.smart_fuel_management_system.service.impl.client;

import com.example.smart_fuel_management_system.dto.VehicleResponseDTO;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JwtService jwtService;

    @Mock
    private ResponseEntity<VehicleResponseDTO> response;

    @InjectMocks
    private VehicleClient vehicleClient;

    @Test
    void resolveVehicle_shouldReturnVehicle_whenVehicleIsFound() {
        // given
        String rfidTag = "RFID123";
        String plateNumber = "27ABC123";
        String token = "service-token";

        VehicleResponseDTO vehicle =
                org.mockito.Mockito.mock(VehicleResponseDTO.class);

        when(jwtService.generateServiceToken(
                "fuel-session-service",
                "vehicle-service",
                "vehicle.internal.resolve"
        )).thenReturn(token);

        when(restTemplate.exchange(
                eq("http://VEHICLE/api/v1/internal/vehicles/resolve?rfidTag={rfidTag}&plateNumber={plateNumber}"),
                eq(HttpMethod.GET),
                org.mockito.ArgumentMatchers.any(HttpEntity.class),
                eq(VehicleResponseDTO.class),
                eq(rfidTag),
                eq(plateNumber)
        )).thenReturn(response);

        when(response.getBody())
                .thenReturn(vehicle);

        // when
        Optional<VehicleResponseDTO> result =
                vehicleClient.resolveVehicle(
                        rfidTag,
                        plateNumber
                );

        // then
        assertThat(result)
                .isPresent()
                .containsSame(vehicle);

        verify(jwtService)
                .generateServiceToken(
                        "fuel-session-service",
                        "vehicle-service",
                        "vehicle.internal.resolve"
                );
    }

    @Test
    void resolveVehicle_shouldReturnEmpty_whenVehicleIsNotFound() {
        // given
        String rfidTag = "RFID123";
        String plateNumber = "27ABC123";

        when(jwtService.generateServiceToken(
                "fuel-session-service",
                "vehicle-service",
                "vehicle.internal.resolve"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                org.mockito.ArgumentMatchers.anyString(),
                eq(HttpMethod.GET),
                org.mockito.ArgumentMatchers.any(HttpEntity.class),
                eq(VehicleResponseDTO.class),
                eq(rfidTag),
                eq(plateNumber)
        )).thenThrow(
                HttpClientErrorException.NotFound.class
        );

        // when
        Optional<VehicleResponseDTO> result =
                vehicleClient.resolveVehicle(
                        rfidTag,
                        plateNumber
                );

        // then
        assertThat(result)
                .isEmpty();
    }

    @Test
    void resolveVehicle_shouldReturnEmpty_whenVehicleServiceThrowsRestClientException() {
        // given
        String rfidTag = "RFID123";
        String plateNumber = "27ABC123";

        when(jwtService.generateServiceToken(
                "fuel-session-service",
                "vehicle-service",
                "vehicle.internal.resolve"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                org.mockito.ArgumentMatchers.anyString(),
                eq(HttpMethod.GET),
                org.mockito.ArgumentMatchers.any(HttpEntity.class),
                eq(VehicleResponseDTO.class),
                eq(rfidTag),
                eq(plateNumber)
        )).thenThrow(
                new RestClientException("Vehicle service unavailable")
        );

        // when
        Optional<VehicleResponseDTO> result =
                vehicleClient.resolveVehicle(
                        rfidTag,
                        plateNumber
                );

        // then
        assertThat(result)
                .isEmpty();
    }

    @Test
    void resolveVehicle_shouldReturnEmpty_whenVehicleServiceIsUnavailable() {
        // given
        String rfidTag = "RFID123";
        String plateNumber = "27ABC123";

        when(jwtService.generateServiceToken(
                "fuel-session-service",
                "vehicle-service",
                "vehicle.internal.resolve"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                org.mockito.ArgumentMatchers.anyString(),
                eq(HttpMethod.GET),
                org.mockito.ArgumentMatchers.any(HttpEntity.class),
                eq(VehicleResponseDTO.class),
                eq(rfidTag),
                eq(plateNumber)
        )).thenThrow(
                new ServiceUnavailableException(
                        "No VEHICLE instance available",
                        new RuntimeException("Vehicle service unavailable")
                )
        );

        // when
        Optional<VehicleResponseDTO> result =
                vehicleClient.resolveVehicle(
                        rfidTag,
                        plateNumber
                );

        // then
        assertThat(result)
                .isEmpty();
    }
}
