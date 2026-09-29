package com.example.smart_fuel_management_system.service.impl.client;


import com.example.smart_fuel_management_system.dto.payment_method.PaymentRequestToUserService;
import com.example.smart_fuel_management_system.dto.payment_method.UserResponseToPaymentService;
import com.example.smart_fuel_management_system.exceptions.ServiceUnavailableException;
import com.example.smart_fuel_management_system.security.JwtService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserClient {

    private static final String BASE_URL = "http://USER/api/v1/internal/users";

    private final RestTemplate restTemplate;

    private final JwtService jwtService;

    public UserResponseToPaymentService getUser(UUID userId){
        HttpEntity<PaymentRequestToUserService> entity =
                new HttpEntity<>(new PaymentRequestToUserService(userId), generateServiceToken());

        try {
            ResponseEntity<UserResponseToPaymentService> response =
                    restTemplate.exchange(
                            BASE_URL + "/" + userId + "/payment",
                            HttpMethod.GET,
                            entity,
                            UserResponseToPaymentService.class
                    );

            return response.getBody();
        }catch (EntityNotFoundException e){
          throw new EntityNotFoundException(
                  "user does not exist!"
          );
        } catch (RestClientException e) {
            throw new ServiceUnavailableException(
                    "Station service is unavailable",
                    e
            );
        } catch (ServiceUnavailableException e) {
            throw new ServiceUnavailableException("No STATION instance available", e);
        }
    }

    private HttpHeaders generateServiceToken(){
        String serviceToken = jwtService.generateServiceToken(
                "payment-service",
                "user-service",
                "user.internal.get-user.payment"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceToken);

        return headers;
    }
}
