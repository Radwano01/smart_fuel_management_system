package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.payment_method.PaymentMethodDetailsResponse;
import com.example.smart_fuel_management_system.dto.payment_method.PaymentMethodRequest;
import com.example.smart_fuel_management_system.entity.PaymentMethod;
import com.example.smart_fuel_management_system.enums.PaymentProvider;
import com.example.smart_fuel_management_system.repository.PaymentMethodRepository;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentMethodServiceImplTest {

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private PaymentMethodServiceImpl paymentMethodService;

    @Test
    void addPaymentMethod_shouldSavePaymentMethod() throws StripeException {

        // given
        UUID userId = UUID.randomUUID();

        PaymentMethodRequest request = mock(PaymentMethodRequest.class);

        when(request.paymentMethodId()).thenReturn("pm_test_123");
        when(customerService.getOrCreateCustomer(userId))
                .thenReturn("cus_test_123");

        when(paymentMethodRepository.existsByUserIdAndCardFingerPrint(
                userId,
                "fingerprint_123"
        )).thenReturn(false);

        when(paymentMethodRepository.existsDefaultByUserId(userId))
                .thenReturn(false);

        com.stripe.model.PaymentMethod stripePaymentMethod =
                mock(com.stripe.model.PaymentMethod.class);

        com.stripe.model.PaymentMethod.Card card =
                mock(com.stripe.model.PaymentMethod.Card.class);

        when(stripePaymentMethod.getId()).thenReturn("pm_test_123");
        when(stripePaymentMethod.getCard()).thenReturn(card);
        when(card.getFingerprint()).thenReturn("fingerprint_123");
        when(card.getBrand()).thenReturn("visa");
        when(card.getLast4()).thenReturn("4242");
        when(card.getExpMonth()).thenReturn(12L);
        when(card.getExpYear()).thenReturn(2030L);

        try (MockedStatic<com.stripe.model.PaymentMethod> paymentMethodMock =
                     mockStatic(com.stripe.model.PaymentMethod.class)) {

            paymentMethodMock.when(() ->
                    com.stripe.model.PaymentMethod.retrieve("pm_test_123")
            ).thenReturn(stripePaymentMethod);

            // when
            paymentMethodService.addPaymentMethod(request, userId);

            // then
            ArgumentCaptor<PaymentMethod> captor =
                    ArgumentCaptor.forClass(PaymentMethod.class);

            verify(paymentMethodRepository).save(captor.capture());

            PaymentMethod savedPaymentMethod = captor.getValue();

            assertThat(savedPaymentMethod.getUserId()).isEqualTo(userId);
            assertThat(savedPaymentMethod.getProvider())
                    .isEqualTo(PaymentProvider.STRIPE);
            assertThat(savedPaymentMethod.getProviderPaymentMethodId())
                    .isEqualTo("pm_test_123");
            assertThat(savedPaymentMethod.getCardFingerPrint())
                    .isEqualTo("fingerprint_123");
            assertThat(savedPaymentMethod.getBrand())
                    .isEqualTo("visa");
            assertThat(savedPaymentMethod.getLast4())
                    .isEqualTo("4242");
            assertThat(savedPaymentMethod.getExpMonth())
                    .isEqualTo(12);
            assertThat(savedPaymentMethod.getExpYear())
                    .isEqualTo(2030);
            assertThat(savedPaymentMethod.isDefault()).isTrue();
        }
    }

    @Test
    void getPaymentMethods_shouldReturnUserPaymentMethods() {

        // given
        UUID userId = UUID.randomUUID();

        PaymentMethod paymentMethod = PaymentMethod.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .provider(PaymentProvider.STRIPE)
                .providerPaymentMethodId("pm_test_123")
                .cardFingerPrint("fingerprint_123")
                .brand("visa")
                .last4("4242")
                .expMonth(12)
                .expYear(2030)
                .isDefault(true)
                .build();

        when(paymentMethodRepository.getByUserId(userId))
                .thenReturn(List.of(paymentMethod));

        // when
        List<PaymentMethodDetailsResponse> result =
                paymentMethodService.getPaymentMethods(userId);

        // then
        assertThat(result).hasSize(1);

        PaymentMethodDetailsResponse response = result.get(0);

        assertThat(response.id()).isEqualTo(paymentMethod.getId());
        assertThat(response.brand()).isEqualTo("visa");
        assertThat(response.last4()).isEqualTo("4242");
        assertThat(response.expMonth()).isEqualTo(12);
        assertThat(response.expYear()).isEqualTo(2030);
        assertThat(response.isDefault()).isTrue();
        assertThat(response.expired()).isFalse();
    }

    @Test
    void setDefault_shouldClearPreviousDefaultAndSetPaymentMethodAsDefault() {

        // given
        UUID userId = UUID.randomUUID();
        UUID paymentMethodId = UUID.randomUUID();

        PaymentMethod paymentMethod = PaymentMethod.builder()
                .id(paymentMethodId)
                .userId(userId)
                .provider(PaymentProvider.STRIPE)
                .providerPaymentMethodId("pm_test_123")
                .isDefault(false)
                .build();

        when(paymentMethodRepository.findByIdAndUserId(
                paymentMethodId,
                userId
        )).thenReturn(Optional.of(paymentMethod));

        // when
        paymentMethodService.setDefault(paymentMethodId, userId);

        // then
        verify(paymentMethodRepository).clearDefaultForUser(userId);
        verify(paymentMethodRepository).save(paymentMethod);

        assertThat(paymentMethod.isDefault()).isTrue();
    }

    @Test
    void setDefault_shouldThrowException_whenPaymentMethodDoesNotExist() {

        // given
        UUID userId = UUID.randomUUID();
        UUID paymentMethodId = UUID.randomUUID();

        when(paymentMethodRepository.findByIdAndUserId(
                paymentMethodId,
                userId
        )).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                paymentMethodService.setDefault(paymentMethodId, userId)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Payment method not found");

        verify(paymentMethodRepository, never())
                .clearDefaultForUser(any());

        verify(paymentMethodRepository, never())
                .save(any());
    }

    @Test
    void addPaymentMethod_shouldThrowException_whenCardAlreadyExists() throws StripeException {

        // given
        UUID userId = UUID.randomUUID();

        PaymentMethodRequest request = mock(PaymentMethodRequest.class);

        when(request.paymentMethodId()).thenReturn("pm_test_123");
        when(customerService.getOrCreateCustomer(userId))
                .thenReturn("cus_test_123");

        when(paymentMethodRepository.existsByUserIdAndCardFingerPrint(
                userId,
                "fingerprint_123"
        )).thenReturn(true);

        com.stripe.model.PaymentMethod stripePaymentMethod =
                mock(com.stripe.model.PaymentMethod.class);

        com.stripe.model.PaymentMethod.Card card =
                mock(com.stripe.model.PaymentMethod.Card.class);

        when(stripePaymentMethod.getCard()).thenReturn(card);
        when(card.getFingerprint()).thenReturn("fingerprint_123");

        try (MockedStatic<com.stripe.model.PaymentMethod> paymentMethodMock =
                     mockStatic(com.stripe.model.PaymentMethod.class)) {

            paymentMethodMock.when(() ->
                    com.stripe.model.PaymentMethod.retrieve("pm_test_123")
            ).thenReturn(stripePaymentMethod);

            // when & then
            assertThatThrownBy(() ->
                    paymentMethodService.addPaymentMethod(request, userId)
            )
                    .isInstanceOf(EntityExistsException.class)
                    .hasMessage("This card already valid in that account!");

            verify(paymentMethodRepository, never()).save(any());
        }
    }

    @Test
    void delete_shouldDetachStripePaymentMethodAndDeletePaymentMethod()
            throws StripeException {

        // given
        UUID userId = UUID.randomUUID();
        UUID paymentMethodId = UUID.randomUUID();

        when(paymentMethodRepository.findStripePaymentMethodIdByIdAndUserId(
                paymentMethodId,
                userId
        )).thenReturn(Optional.of("pm_test_123"));

        com.stripe.model.PaymentMethod stripePaymentMethod =
                mock(com.stripe.model.PaymentMethod.class);

        try (MockedStatic<com.stripe.model.PaymentMethod> paymentMethodMock =
                     mockStatic(com.stripe.model.PaymentMethod.class)) {

            paymentMethodMock.when(() ->
                    com.stripe.model.PaymentMethod.retrieve("pm_test_123")
            ).thenReturn(stripePaymentMethod);

            // when
            paymentMethodService.delete(paymentMethodId, userId);

            // then
            verify(stripePaymentMethod).detach();

            verify(paymentMethodRepository)
                    .deleteByIdAndUserId(paymentMethodId, userId);
        }
    }
}
