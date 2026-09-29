package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.payment_method.UserResponseToPaymentService;
import com.example.smart_fuel_management_system.entity.PaymentCustomer;
import com.example.smart_fuel_management_system.enums.PaymentProvider;
import com.example.smart_fuel_management_system.repository.PaymentCustomerRepository;
import com.example.smart_fuel_management_system.service.impl.client.UserClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private PaymentCustomerRepository paymentCustomerRepository;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void getOrCreateCustomer_shouldReturnExistingCustomer_whenCustomerExists() {

        // given
        UUID userId = UUID.randomUUID();

        PaymentCustomer paymentCustomer = PaymentCustomer.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .provider(PaymentProvider.STRIPE)
                .providerCustomerId("cus_existing")
                .build();

        when(paymentCustomerRepository.findByUserIdAndProvider(
                userId,
                PaymentProvider.STRIPE
        )).thenReturn(Optional.of(paymentCustomer));

        // when
        String result = customerService.getOrCreateCustomer(userId);

        // then
        assertThat(result).isEqualTo("cus_existing");

        verify(paymentCustomerRepository)
                .findByUserIdAndProvider(userId, PaymentProvider.STRIPE);

        verifyNoInteractions(userClient);
    }

    @Test
    void getOrCreateCustomer_shouldCreateCustomer_whenCustomerDoesNotExist()
            throws StripeException {

        // given
        UUID userId = UUID.randomUUID();

        UserResponseToPaymentService user =
                mock(UserResponseToPaymentService.class);

        when(paymentCustomerRepository.findByUserIdAndProvider(
                userId,
                PaymentProvider.STRIPE
        )).thenReturn(Optional.empty());

        when(userClient.getUser(userId)).thenReturn(user);
        when(user.email()).thenReturn("test@gmail.com");
        when(user.fullName()).thenReturn("Radwan Rahmoun");

        Customer stripeCustomer = mock(Customer.class);
        when(stripeCustomer.getId()).thenReturn("cus_test_123");

        try (MockedStatic<Customer> customerMock =
                     org.mockito.Mockito.mockStatic(Customer.class)) {

            customerMock.when(() ->
                    Customer.create(
                            org.mockito.ArgumentMatchers.any(CustomerCreateParams.class)
                    )
            ).thenReturn(stripeCustomer);

            // when
            String result = customerService.getOrCreateCustomer(userId);

            // then
            assertThat(result).isEqualTo("cus_test_123");

            ArgumentCaptor<PaymentCustomer> captor =
                    ArgumentCaptor.forClass(PaymentCustomer.class);

            verify(paymentCustomerRepository).save(captor.capture());

            PaymentCustomer savedCustomer = captor.getValue();

            assertThat(savedCustomer.getUserId()).isEqualTo(userId);
            assertThat(savedCustomer.getProvider())
                    .isEqualTo(PaymentProvider.STRIPE);
            assertThat(savedCustomer.getProviderCustomerId())
                    .isEqualTo("cus_test_123");
        }
    }

    @Test
    void getOrCreateCustomer_shouldThrowRuntimeException_whenStripeCustomerCreationFails()
            throws StripeException {

        // given
        UUID userId = UUID.randomUUID();

        UserResponseToPaymentService user =
                mock(UserResponseToPaymentService.class);

        when(paymentCustomerRepository.findByUserIdAndProvider(
                userId,
                PaymentProvider.STRIPE
        )).thenReturn(Optional.empty());

        when(userClient.getUser(userId)).thenReturn(user);
        when(user.email()).thenReturn("test@gmail.com");
        when(user.fullName()).thenReturn("Radwan Rahmoun");

        StripeException stripeException = mock(StripeException.class);

        try (MockedStatic<Customer> customerMock =
                     org.mockito.Mockito.mockStatic(Customer.class)) {

            customerMock.when(() ->
                    Customer.create(
                            org.mockito.ArgumentMatchers.any(CustomerCreateParams.class)
                    )
            ).thenThrow(stripeException);

            // when & then
            assertThatThrownBy(() ->
                    customerService.getOrCreateCustomer(userId)
            )
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Failed to create Stripe customer")
                    .hasCause(stripeException);

            verify(paymentCustomerRepository)
                    .findByUserIdAndProvider(
                            userId,
                            PaymentProvider.STRIPE
                    );

            verify(paymentCustomerRepository, org.mockito.Mockito.never())
                    .save(org.mockito.ArgumentMatchers.any());
        }
    }
}
