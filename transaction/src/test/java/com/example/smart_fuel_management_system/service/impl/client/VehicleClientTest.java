package com.example.smart_fuel_management_system.service.impl.client;

import com.example.smart_fuel_management_system.dto.VehicleTransactionResponse;
import com.example.smart_fuel_management_system.exceptions.ServiceUnavailableException;
import com.example.smart_fuel_management_system.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private VehicleClient vehicleClient;

    private UUID vehicleId;
    private UUID anotherVehicleId;

    @BeforeEach
    void setUp() {
        vehicleId = UUID.randomUUID();
        anotherVehicleId = UUID.randomUUID();
    }

    @Test
    void getVehicleDetails_returnsBodyWhenResponseHasBody() {
        // given
        VehicleTransactionResponse body = mock(VehicleTransactionResponse.class);
        when(jwtService.generateServiceToken(
                "transaction-service", "vehicle-service", "vehicle.internal.read"))
                .thenReturn("token");
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(VehicleTransactionResponse.class), eq(vehicleId)))
                .thenReturn(ResponseEntity.ok(body));

        // when
        Optional<VehicleTransactionResponse> result =
                vehicleClient.getVehicleDetails(vehicleId);

        // then
        assertThat(result).containsSame(body);
    }

    @Test
    void getVehicleDetails_returnsEmptyWhenBodyIsNull() {
        // given
        when(jwtService.generateServiceToken(
                "transaction-service", "vehicle-service", "vehicle.internal.read"))
                .thenReturn("token");
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(VehicleTransactionResponse.class), eq(vehicleId)))
                .thenReturn(ResponseEntity.ok(null));

        // when
        Optional<VehicleTransactionResponse> result =
                vehicleClient.getVehicleDetails(vehicleId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getVehicleDetails_returnsEmptyOnNotFound() {
        // given
        when(jwtService.generateServiceToken(
                "transaction-service", "vehicle-service", "vehicle.internal.read"))
                .thenReturn("token");
        HttpClientErrorException.NotFound notFound =
                (HttpClientErrorException.NotFound) HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(VehicleTransactionResponse.class), eq(vehicleId)))
                .thenThrow(notFound);

        // when
        Optional<VehicleTransactionResponse> result =
                vehicleClient.getVehicleDetails(vehicleId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getVehicleDetails_returnsEmptyOnRestClientException() {
        // given
        when(jwtService.generateServiceToken(
                "transaction-service", "vehicle-service", "vehicle.internal.read"))
                .thenReturn("token");
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(VehicleTransactionResponse.class), eq(vehicleId)))
                .thenThrow(new ResourceAccessException("connection refused"));

        // when
        Optional<VehicleTransactionResponse> result =
                vehicleClient.getVehicleDetails(vehicleId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getVehicleDetails_returnsEmptyOnServiceUnavailable() {
        // given
        when(jwtService.generateServiceToken(
                "transaction-service", "vehicle-service", "vehicle.internal.read"))
                .thenReturn("token");
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(VehicleTransactionResponse.class), eq(vehicleId)))
                .thenThrow(new ServiceUnavailableException("no instance", null));

        // when
        Optional<VehicleTransactionResponse> result =
                vehicleClient.getVehicleDetails(vehicleId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getVehicleDetails_sendsBearerToken() {
        // given
        when(jwtService.generateServiceToken(
                "transaction-service", "vehicle-service", "vehicle.internal.read"))
                .thenReturn("service-token");
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(VehicleTransactionResponse.class), eq(vehicleId)))
                .thenReturn(ResponseEntity.ok(null));

        // when
        vehicleClient.getVehicleDetails(vehicleId);

        // then
        ArgumentCaptor<HttpEntity<Void>> captor =
                ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                anyString(), eq(HttpMethod.GET), captor.capture(),
                eq(VehicleTransactionResponse.class), eq(vehicleId));

        String authorization = captor.getValue().getHeaders().getFirst("Authorization");
        assertThat(authorization).isEqualTo("Bearer service-token");
    }


    @Test
    void getVehiclesDetails_returnsEmptyListWhenIdsNull() {
        // given / when
        List<VehicleTransactionResponse> result =
                vehicleClient.getVehiclesDetails(null);

        // then
        assertThat(result).isEmpty();
        verify(restTemplate, never()).exchange(
                anyString(), any(), any(), any(ParameterizedTypeReference.class));
    }

    @Test
    void getVehiclesDetails_returnsEmptyListWhenIdsEmpty() {
        // given / when
        List<VehicleTransactionResponse> result =
                vehicleClient.getVehiclesDetails(List.of());

        // then
        assertThat(result).isEmpty();
        verify(restTemplate, never()).exchange(
                anyString(), any(), any(), any(ParameterizedTypeReference.class));
    }

    @Test
    void getVehiclesDetails_returnsBodyWhenResponseHasBody() {
        // given
        VehicleTransactionResponse first = mock(VehicleTransactionResponse.class);
        VehicleTransactionResponse second = mock(VehicleTransactionResponse.class);
        List<VehicleTransactionResponse> body = List.of(first, second);

        when(jwtService.generateServiceToken(
                "transaction-service", "vehicle-service", "vehicle.internal.read"))
                .thenReturn("token");
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(body));

        // when
        List<VehicleTransactionResponse> result =
                vehicleClient.getVehiclesDetails(List.of(vehicleId, anotherVehicleId));

        // then
        assertThat(result).containsExactly(first, second);
    }

    @Test
    void getVehiclesDetails_returnsEmptyListWhenBodyIsNull() {
        // given
        when(jwtService.generateServiceToken(
                "transaction-service", "vehicle-service", "vehicle.internal.read"))
                .thenReturn("token");
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(null));

        // when
        List<VehicleTransactionResponse> result =
                vehicleClient.getVehiclesDetails(List.of(vehicleId));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getVehiclesDetails_returnsEmptyListOnNotFound() {
        // given
        when(jwtService.generateServiceToken(
                "transaction-service", "vehicle-service", "vehicle.internal.read"))
                .thenReturn("token");
        HttpClientErrorException.NotFound notFound =
                (HttpClientErrorException.NotFound) HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenThrow(notFound);

        // when
        List<VehicleTransactionResponse> result =
                vehicleClient.getVehiclesDetails(List.of(vehicleId));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getVehiclesDetails_returnsEmptyListOnRestClientException() {
        // given
        when(jwtService.generateServiceToken(
                "transaction-service", "vehicle-service", "vehicle.internal.read"))
                .thenReturn("token");
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenThrow(new ResourceAccessException("connection refused"));

        // when
        List<VehicleTransactionResponse> result =
                vehicleClient.getVehiclesDetails(List.of(vehicleId));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getVehiclesDetails_returnsEmptyListOnServiceUnavailable() {
        // given
        when(jwtService.generateServiceToken(
                "transaction-service", "vehicle-service", "vehicle.internal.read"))
                .thenReturn("token");
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenThrow(new ServiceUnavailableException("no instance", null));

        // when
        List<VehicleTransactionResponse> result =
                vehicleClient.getVehiclesDetails(List.of(vehicleId));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getVehiclesDetails_sendsIdsAsBodyAndBearerToken() {
        // given
        List<UUID> ids = List.of(vehicleId, anotherVehicleId);
        when(jwtService.generateServiceToken(
                "transaction-service", "vehicle-service", "vehicle.internal.read"))
                .thenReturn("service-token");
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(List.of()));

        // when
        vehicleClient.getVehiclesDetails(ids);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<List<UUID>>> captor =
                ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                anyString(), eq(HttpMethod.POST), captor.capture(),
                any(ParameterizedTypeReference.class));

        HttpEntity<List<UUID>> sent = captor.getValue();
        assertThat(sent.getBody()).containsExactlyElementsOf(ids);
        assertThat(sent.getHeaders().getFirst("Authorization"))
                .isEqualTo("Bearer service-token");
    }
}