package com.example.smart_fuel_management_system.service.impl.client;

import com.example.smart_fuel_management_system.dto.station.TransactionStationResponse;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionClientTest {

    private static final String BASE_URL =
            "http://TRANSACTION/api/v1/internal/transactions";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private TransactionClient transactionClient;

    private UUID stationId;
    private UUID anotherStationId;

    @BeforeEach
    void setUp() {
        stationId = UUID.randomUUID();
        anotherStationId = UUID.randomUUID();
    }

    @Test
    void getTransactionsAndVehiclesCount_shouldReturnBody_whenSingleStationExists() {
        // given
        TransactionStationResponse body = mock(TransactionStationResponse.class);
        when(jwtService.generateServiceToken(
                "station-service", "transaction-service", "user.internal.station-count.get"))
                .thenReturn("token");
        when(restTemplate.exchange(
                eq(BASE_URL + "/stations/" + stationId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(TransactionStationResponse.class)))
                .thenReturn(ResponseEntity.ok(body));

        // when
        Optional<TransactionStationResponse> result =
                transactionClient.getTransactionsAndVehiclesCount(stationId);

        // then
        assertThat(result).containsSame(body);
    }

    @Test
    void getTransactionsAndVehiclesCount_shouldReturnEmpty_whenSingleBodyIsNull() {
        // given
        when(jwtService.generateServiceToken(
                "station-service", "transaction-service", "user.internal.station-count.get"))
                .thenReturn("token");
        when(restTemplate.exchange(
                eq(BASE_URL + "/stations/" + stationId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(TransactionStationResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        // when
        Optional<TransactionStationResponse> result =
                transactionClient.getTransactionsAndVehiclesCount(stationId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getTransactionsAndVehiclesCount_shouldReturnEmptyOnNotFound_whenSingleStationMissing() {
        // given
        when(jwtService.generateServiceToken(
                "station-service", "transaction-service", "user.internal.station-count.get"))
                .thenReturn("token");
        HttpClientErrorException.NotFound notFound =
                (HttpClientErrorException.NotFound) HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        when(restTemplate.exchange(
                eq(BASE_URL + "/stations/" + stationId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(TransactionStationResponse.class)))
                .thenThrow(notFound);

        // when
        Optional<TransactionStationResponse> result =
                transactionClient.getTransactionsAndVehiclesCount(stationId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getTransactionsAndVehiclesCount_shouldReturnEmptyOnRestClientException_whenSingle() {
        // given
        when(jwtService.generateServiceToken(
                "station-service", "transaction-service", "user.internal.station-count.get"))
                .thenReturn("token");
        when(restTemplate.exchange(
                eq(BASE_URL + "/stations/" + stationId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(TransactionStationResponse.class)))
                .thenThrow(new ResourceAccessException("connection refused"));

        // when
        Optional<TransactionStationResponse> result =
                transactionClient.getTransactionsAndVehiclesCount(stationId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getTransactionsAndVehiclesCount_shouldReturnEmptyOnServiceUnavailable_whenSingle() {
        // given
        when(jwtService.generateServiceToken(
                "station-service", "transaction-service", "user.internal.station-count.get"))
                .thenReturn("token");
        when(restTemplate.exchange(
                eq(BASE_URL + "/stations/" + stationId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(TransactionStationResponse.class)))
                .thenThrow(new ServiceUnavailableException("no instance", null));

        // when
        Optional<TransactionStationResponse> result =
                transactionClient.getTransactionsAndVehiclesCount(stationId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getTransactionsAndVehiclesCount_shouldSendBearerToken_whenSingle() {
        // given
        when(jwtService.generateServiceToken(
                "station-service", "transaction-service", "user.internal.station-count.get"))
                .thenReturn("service-token");
        when(restTemplate.exchange(
                eq(BASE_URL + "/stations/" + stationId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(TransactionStationResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        // when
        transactionClient.getTransactionsAndVehiclesCount(stationId);

        // then
        ArgumentCaptor<HttpEntity<Void>> captor =
                ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                eq(BASE_URL + "/stations/" + stationId),
                eq(HttpMethod.GET),
                captor.capture(),
                eq(TransactionStationResponse.class));

        String authorization = captor.getValue().getHeaders().getFirst("Authorization");
        assertThat(authorization).isEqualTo("Bearer service-token");
    }

    @Test
    void getTransactionsAndVehiclesCount_shouldReturnEmptyList_whenIdsNull() {
        // when
        List<TransactionStationResponse> result =
                transactionClient.getTransactionsAndVehiclesCount((List<UUID>) null);

        // then
        assertThat(result).isEmpty();
        verify(restTemplate, never()).exchange(
                any(String.class), any(), any(), any(ParameterizedTypeReference.class));
    }

    @Test
    void getTransactionsAndVehiclesCount_shouldReturnEmptyList_whenIdsEmpty() {
        // when
        List<TransactionStationResponse> result =
                transactionClient.getTransactionsAndVehiclesCount(List.of());

        // then
        assertThat(result).isEmpty();
        verify(restTemplate, never()).exchange(
                any(String.class), any(), any(), any(ParameterizedTypeReference.class));
    }

    @Test
    void getTransactionsAndVehiclesCount_shouldReturnBody_whenMultipleStationsExist() {
        // given
        TransactionStationResponse first = mock(TransactionStationResponse.class);
        TransactionStationResponse second = mock(TransactionStationResponse.class);
        List<TransactionStationResponse> body = List.of(first, second);

        when(jwtService.generateServiceToken(
                "station-service", "transaction-service", "user.internal.station-count.get"))
                .thenReturn("token");
        when(restTemplate.exchange(
                eq(BASE_URL + "/stations/by-ids"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(body));

        // when
        List<TransactionStationResponse> result =
                transactionClient.getTransactionsAndVehiclesCount(
                        List.of(stationId, anotherStationId));

        // then
        assertThat(result).containsExactly(first, second);
    }

    @Test
    void getTransactionsAndVehiclesCount_shouldReturnEmptyList_whenMultipleBodyIsNull() {
        // given
        when(jwtService.generateServiceToken(
                "station-service", "transaction-service", "user.internal.station-count.get"))
                .thenReturn("token");
        when(restTemplate.exchange(
                eq(BASE_URL + "/stations/by-ids"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(null));

        // when
        List<TransactionStationResponse> result =
                transactionClient.getTransactionsAndVehiclesCount(List.of(stationId));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getTransactionsAndVehiclesCount_shouldReturnEmptyListOnNotFound_whenMultiple() {
        // given
        when(jwtService.generateServiceToken(
                "station-service", "transaction-service", "user.internal.station-count.get"))
                .thenReturn("token");
        HttpClientErrorException.NotFound notFound =
                (HttpClientErrorException.NotFound) HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        when(restTemplate.exchange(
                eq(BASE_URL + "/stations/by-ids"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenThrow(notFound);

        // when
        List<TransactionStationResponse> result =
                transactionClient.getTransactionsAndVehiclesCount(List.of(stationId));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getTransactionsAndVehiclesCount_shouldReturnEmptyListOnRestClientException_whenMultiple() {
        // given
        when(jwtService.generateServiceToken(
                "station-service", "transaction-service", "user.internal.station-count.get"))
                .thenReturn("token");
        when(restTemplate.exchange(
                eq(BASE_URL + "/stations/by-ids"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenThrow(new ResourceAccessException("connection refused"));

        // when
        List<TransactionStationResponse> result =
                transactionClient.getTransactionsAndVehiclesCount(List.of(stationId));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getTransactionsAndVehiclesCount_shouldReturnEmptyListOnIllegalArgument_whenMultiple() {
        // given
        when(jwtService.generateServiceToken(
                "station-service", "transaction-service", "user.internal.station-count.get"))
                .thenReturn("token");
        when(restTemplate.exchange(
                eq(BASE_URL + "/stations/by-ids"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenThrow(new IllegalArgumentException("no instance"));

        // when
        List<TransactionStationResponse> result =
                transactionClient.getTransactionsAndVehiclesCount(List.of(stationId));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getTransactionsAndVehiclesCount_shouldSendIdsAsBodyAndBearerToken_whenMultiple() {
        // given
        List<UUID> ids = List.of(stationId, anotherStationId);
        when(jwtService.generateServiceToken(
                "station-service", "transaction-service", "user.internal.station-count.get"))
                .thenReturn("service-token");
        when(restTemplate.exchange(
                eq(BASE_URL + "/stations/by-ids"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(List.of()));

        // when
        transactionClient.getTransactionsAndVehiclesCount(ids);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<List<UUID>>> captor =
                ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                eq(BASE_URL + "/stations/by-ids"),
                eq(HttpMethod.POST),
                captor.capture(),
                any(ParameterizedTypeReference.class));

        HttpEntity<List<UUID>> sent = captor.getValue();
        assertThat(sent.getBody()).containsExactlyElementsOf(ids);
        assertThat(sent.getHeaders().getFirst("Authorization"))
                .isEqualTo("Bearer service-token");
    }
}