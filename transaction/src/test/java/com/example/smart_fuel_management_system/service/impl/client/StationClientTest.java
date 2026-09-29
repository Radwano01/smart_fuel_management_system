package com.example.smart_fuel_management_system.service.impl.client;

import com.example.smart_fuel_management_system.dto.StationTransactionResponse;
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
class StationClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private StationClient stationClient;

    private UUID stationId;
    private UUID anotherStationId;

    @BeforeEach
    void setUp() {
        stationId = UUID.randomUUID();
        anotherStationId = UUID.randomUUID();
    }

    @Test
    void getStationDetails_returnsBodyWhenResponseHasBody() {
        // given
        StationTransactionResponse body = mock(StationTransactionResponse.class);
        when(jwtService.generateServiceToken(
                "transaction-service", "station-service", "station.internal.read"))
                .thenReturn("token");
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(StationTransactionResponse.class), eq(stationId)))
                .thenReturn(ResponseEntity.ok(body));

        // when
        Optional<StationTransactionResponse> result =
                stationClient.getStationDetails(stationId);

        // then
        assertThat(result).containsSame(body);
    }

    @Test
    void getStationDetails_returnsEmptyWhenBodyIsNull() {
        // given
        when(jwtService.generateServiceToken(
                "transaction-service", "station-service", "station.internal.read"))
                .thenReturn("token");
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(StationTransactionResponse.class), eq(stationId)))
                .thenReturn(ResponseEntity.ok(null));

        // when
        Optional<StationTransactionResponse> result =
                stationClient.getStationDetails(stationId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getStationDetails_returnsEmptyOnNotFound() {
        // given
        when(jwtService.generateServiceToken(
                "transaction-service", "station-service", "station.internal.read"))
                .thenReturn("token");
        HttpClientErrorException.NotFound notFound =
                (HttpClientErrorException.NotFound) HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(StationTransactionResponse.class), eq(stationId)))
                .thenThrow(notFound);

        // when
        Optional<StationTransactionResponse> result =
                stationClient.getStationDetails(stationId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getStationDetails_returnsEmptyOnRestClientException() {
        // given
        when(jwtService.generateServiceToken(
                "transaction-service", "station-service", "station.internal.read"))
                .thenReturn("token");
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(StationTransactionResponse.class), eq(stationId)))
                .thenThrow(new ResourceAccessException("connection refused"));

        // when
        Optional<StationTransactionResponse> result =
                stationClient.getStationDetails(stationId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getStationDetails_returnsEmptyOnIllegalArgument() {
        // given
        when(jwtService.generateServiceToken(
                "transaction-service", "station-service", "station.internal.read"))
                .thenReturn("token");
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(StationTransactionResponse.class), eq(stationId)))
                .thenThrow(new IllegalArgumentException("no instance"));

        // when
        Optional<StationTransactionResponse> result =
                stationClient.getStationDetails(stationId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getStationDetails_sendsBearerToken() {
        // given
        when(jwtService.generateServiceToken(
                "transaction-service", "station-service", "station.internal.read"))
                .thenReturn("service-token");
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(StationTransactionResponse.class), eq(stationId)))
                .thenReturn(ResponseEntity.ok(null));

        // when
        stationClient.getStationDetails(stationId);

        // then
        ArgumentCaptor<HttpEntity<Void>> captor =
                ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                anyString(), eq(HttpMethod.GET), captor.capture(),
                eq(StationTransactionResponse.class), eq(stationId));

        String authorization = captor.getValue().getHeaders().getFirst("Authorization");
        assertThat(authorization).isEqualTo("Bearer service-token");
    }


    @Test
    void getStationsDetails_returnsEmptyListWhenIdsNull() {
        // given / when
        List<StationTransactionResponse> result =
                stationClient.getStationsDetails(null);

        // then
        assertThat(result).isEmpty();
        verify(restTemplate, never()).exchange(
                anyString(), any(), any(), any(ParameterizedTypeReference.class));
    }

    @Test
    void getStationsDetails_returnsEmptyListWhenIdsEmpty() {
        // given / when
        List<StationTransactionResponse> result =
                stationClient.getStationsDetails(List.of());

        // then
        assertThat(result).isEmpty();
        verify(restTemplate, never()).exchange(
                anyString(), any(), any(), any(ParameterizedTypeReference.class));
    }

    @Test
    void getStationsDetails_returnsBodyWhenResponseHasBody() {
        // given
        StationTransactionResponse first = mock(StationTransactionResponse.class);
        StationTransactionResponse second = mock(StationTransactionResponse.class);
        List<StationTransactionResponse> body = List.of(first, second);

        when(jwtService.generateServiceToken(
                "transaction-service", "station-service", "station.internal.read"))
                .thenReturn("token");
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(body));

        // when
        List<StationTransactionResponse> result =
                stationClient.getStationsDetails(List.of(stationId, anotherStationId));

        // then
        assertThat(result).containsExactly(first, second);
    }

    @Test
    void getStationsDetails_returnsEmptyListWhenBodyIsNull() {
        // given
        when(jwtService.generateServiceToken(
                "transaction-service", "station-service", "station.internal.read"))
                .thenReturn("token");
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(null));

        // when
        List<StationTransactionResponse> result =
                stationClient.getStationsDetails(List.of(stationId));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getStationsDetails_returnsEmptyListOnNotFound() {
        // given
        when(jwtService.generateServiceToken(
                "transaction-service", "station-service", "station.internal.read"))
                .thenReturn("token");
        HttpClientErrorException.NotFound notFound =
                (HttpClientErrorException.NotFound) HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenThrow(notFound);

        // when
        List<StationTransactionResponse> result =
                stationClient.getStationsDetails(List.of(stationId));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getStationsDetails_returnsEmptyListOnRestClientException() {
        // given
        when(jwtService.generateServiceToken(
                "transaction-service", "station-service", "station.internal.read"))
                .thenReturn("token");
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenThrow(new ResourceAccessException("connection refused"));

        // when
        List<StationTransactionResponse> result =
                stationClient.getStationsDetails(List.of(stationId));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getStationsDetails_returnsEmptyListOnIllegalArgument() {
        // given
        when(jwtService.generateServiceToken(
                "transaction-service", "station-service", "station.internal.read"))
                .thenReturn("token");
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenThrow(new IllegalArgumentException("no instance"));

        // when
        List<StationTransactionResponse> result =
                stationClient.getStationsDetails(List.of(stationId));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getStationsDetails_sendsIdsAsBodyAndBearerToken() {
        // given
        List<UUID> ids = List.of(stationId, anotherStationId);
        when(jwtService.generateServiceToken(
                "transaction-service", "station-service", "station.internal.read"))
                .thenReturn("service-token");
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(List.of()));

        // when
        stationClient.getStationsDetails(ids);

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