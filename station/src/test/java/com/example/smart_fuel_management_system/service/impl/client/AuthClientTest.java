package com.example.smart_fuel_management_system.service.impl.client;

import com.example.smart_fuel_management_system.dto.station.StationEmployeeResponse;
import com.example.smart_fuel_management_system.exceptions.ServiceUnavailableException;
import com.example.smart_fuel_management_system.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthClientTest {

    private static final String BASE_URL = "http://AUTH/api/v1/internal/auth";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthClient authClient;

    private UUID employeeId;
    private String expectedUrl;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        expectedUrl = BASE_URL + "/stations/" + employeeId + "/employee";
    }

    @Test
    void getEmployeeInfo_shouldReturnBody_whenResponseHasBody() {
        // given
        StationEmployeeResponse body = mock(StationEmployeeResponse.class);
        when(jwtService.generateServiceToken(
                "station-service", "auth-service", "auth.internal.station-employee.get"))
                .thenReturn("token");
        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(StationEmployeeResponse.class)))
                .thenReturn(ResponseEntity.ok(body));

        // when
        Optional<StationEmployeeResponse> result = authClient.getEmployeeInfo(employeeId);

        // then
        assertThat(result).containsSame(body);
    }

    @Test
    void getEmployeeInfo_shouldReturnEmpty_whenBodyIsNull() {
        // given
        when(jwtService.generateServiceToken(
                "station-service", "auth-service", "auth.internal.station-employee.get"))
                .thenReturn("token");
        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(StationEmployeeResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        // when
        Optional<StationEmployeeResponse> result = authClient.getEmployeeInfo(employeeId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getEmployeeInfo_shouldReturnEmptyOnNotFound() {
        // given
        when(jwtService.generateServiceToken(
                "station-service", "auth-service", "auth.internal.station-employee.get"))
                .thenReturn("token");
        HttpClientErrorException.NotFound notFound =
                (HttpClientErrorException.NotFound) HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(StationEmployeeResponse.class)))
                .thenThrow(notFound);

        // when
        Optional<StationEmployeeResponse> result = authClient.getEmployeeInfo(employeeId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getEmployeeInfo_shouldReturnEmptyOnRestClientException() {
        // given
        when(jwtService.generateServiceToken(
                "station-service", "auth-service", "auth.internal.station-employee.get"))
                .thenReturn("token");
        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(StationEmployeeResponse.class)))
                .thenThrow(new ResourceAccessException("connection refused"));

        // when
        Optional<StationEmployeeResponse> result = authClient.getEmployeeInfo(employeeId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getEmployeeInfo_shouldReturnEmptyOnServiceUnavailable() {
        // given
        when(jwtService.generateServiceToken(
                "station-service", "auth-service", "auth.internal.station-employee.get"))
                .thenReturn("token");
        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(StationEmployeeResponse.class)))
                .thenThrow(new ServiceUnavailableException("no instance", null));

        // when
        Optional<StationEmployeeResponse> result = authClient.getEmployeeInfo(employeeId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getEmployeeInfo_shouldSendBearerToken() {
        // given
        when(jwtService.generateServiceToken(
                "station-service", "auth-service", "auth.internal.station-employee.get"))
                .thenReturn("service-token");
        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(StationEmployeeResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        // when
        authClient.getEmployeeInfo(employeeId);

        // then
        ArgumentCaptor<HttpEntity<Void>> captor =
                ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                captor.capture(),
                eq(StationEmployeeResponse.class));

        String authorization = captor.getValue().getHeaders().getFirst("Authorization");
        assertThat(authorization).isEqualTo("Bearer service-token");
    }

    @Test
    void getEmployeeInfo_shouldUseExpectedUrl() {
        // given
        when(jwtService.generateServiceToken(
                "station-service", "auth-service", "auth.internal.station-employee.get"))
                .thenReturn("token");
        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(StationEmployeeResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        // when
        authClient.getEmployeeInfo(employeeId);

        // then
        verify(restTemplate).exchange(
                eq("http://AUTH/api/v1/internal/auth/stations/" + employeeId + "/employee"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(StationEmployeeResponse.class));
    }
}