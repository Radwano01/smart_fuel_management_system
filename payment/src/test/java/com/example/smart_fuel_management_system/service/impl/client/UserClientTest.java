package com.example.smart_fuel_management_system.service.impl.client;

import com.example.smart_fuel_management_system.dto.payment_method.UserResponseToPaymentService;
import com.example.smart_fuel_management_system.exceptions.ServiceUnavailableException;
import com.example.smart_fuel_management_system.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserClient userClient;

    @Test
    void getUser_shouldReturnUser_whenRequestSucceeds() {
        // given
        UUID userId = UUID.randomUUID();

        UserResponseToPaymentService response =
                new UserResponseToPaymentService(
                        "Radwan Rahmoun",
                        "test@gmail.com"
                );

        when(jwtService.generateServiceToken(
                "payment-service",
                "user-service",
                "user.internal.get-user.payment"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                eq("http://USER/api/v1/internal/users/" + userId + "/payment"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UserResponseToPaymentService.class)
        )).thenReturn(ResponseEntity.ok(response));

        // when
        UserResponseToPaymentService result = userClient.getUser(userId);

        // then
        assertThat(result).isEqualTo(response);

        verify(jwtService).generateServiceToken(
                "payment-service",
                "user-service",
                "user.internal.get-user.payment"
        );
    }

    @Test
    void getUser_shouldSendServiceTokenInAuthorizationHeader() {
        // given
        UUID userId = UUID.randomUUID();

        UserResponseToPaymentService response =
                new UserResponseToPaymentService(
                        "Radwan Rahmoun",
                        "test@gmail.com"
                );

        when(jwtService.generateServiceToken(
                "payment-service",
                "user-service",
                "user.internal.get-user.payment"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                eq("http://USER/api/v1/internal/users/" + userId + "/payment"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UserResponseToPaymentService.class)
        )).thenReturn(ResponseEntity.ok(response));

        ArgumentCaptor<HttpEntity> entityCaptor =
                ArgumentCaptor.forClass(HttpEntity.class);

        // when
        userClient.getUser(userId);

        // then
        verify(restTemplate).exchange(
                eq("http://USER/api/v1/internal/users/" + userId + "/payment"),
                eq(HttpMethod.GET),
                entityCaptor.capture(),
                eq(UserResponseToPaymentService.class)
        );

        HttpHeaders headers = entityCaptor.getValue().getHeaders();

        assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION))
                .isEqualTo("Bearer service-token");
    }

    @Test
    void getUser_shouldThrowServiceUnavailableException_whenRestClientFails() {
        // given
        UUID userId = UUID.randomUUID();

        when(jwtService.generateServiceToken(
                "payment-service",
                "user-service",
                "user.internal.get-user.payment"
        )).thenReturn("service-token");

        when(restTemplate.exchange(
                eq("http://USER/api/v1/internal/users/" + userId + "/payment"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UserResponseToPaymentService.class)
        )).thenThrow(new RestClientException("Connection failed"));

        // when & then
        assertThatThrownBy(() -> userClient.getUser(userId))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessage("Station service is unavailable");
    }
}
