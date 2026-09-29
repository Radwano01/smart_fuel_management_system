package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.payment_method.UserResponseToPaymentService;
import com.example.smart_fuel_management_system.entity.PaymentCustomer;
import com.example.smart_fuel_management_system.enums.PaymentProvider;
import com.example.smart_fuel_management_system.repository.PaymentCustomerRepository;
import com.example.smart_fuel_management_system.service.impl.client.UserClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final PaymentCustomerRepository paymentCustomerRepository;
    private final UserClient userClient;

    // TODO: Resolve the PaymentProvider dynamically and delegate to the appropriate
    //  PaymentGateway implementation (Stripe, PayPal, Adyen, etc.).
    public String getOrCreateCustomer(UUID userId) {

        return paymentCustomerRepository
                .findByUserIdAndProvider(userId, PaymentProvider.STRIPE)
                .map(PaymentCustomer::getProviderCustomerId)
                .orElseGet(() -> createStripeCustomer(userId));
    }

    private String createStripeCustomer(UUID userId) {

        try {
            UserResponseToPaymentService user = userClient.getUser(userId);

            Customer customer = Customer.create(
                    CustomerCreateParams.builder()
                            .setEmail(user.email())
                            .setName(user.fullName())
                            .build()
            );

            paymentCustomerRepository.save(
                    PaymentCustomer.builder()
                            .id(UUID.randomUUID())
                            .userId(userId)
                            .provider(PaymentProvider.STRIPE)
                            .providerCustomerId(customer.getId())
                            .build()
            );

            return customer.getId();

        } catch (StripeException e) {
            throw new RuntimeException("Failed to create Stripe customer", e);
        }
    }
}