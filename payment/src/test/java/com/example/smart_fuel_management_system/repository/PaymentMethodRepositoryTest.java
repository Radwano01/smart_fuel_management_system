package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.entity.PaymentMethod;
import com.example.smart_fuel_management_system.enums.PaymentProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class PaymentMethodRepositoryTest {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Test
    void getByUserId_shouldReturnPaymentMethods_forUser() {

        // given
        UUID userId = UUID.randomUUID();

        PaymentMethod first = createPaymentMethod(userId, "pm_1", true);
        PaymentMethod second = createPaymentMethod(userId, "pm_2", false);
        PaymentMethod otherUser = createPaymentMethod(UUID.randomUUID(), "pm_3", true);

        paymentMethodRepository.save(first);
        paymentMethodRepository.save(second);
        paymentMethodRepository.saveAndFlush(otherUser);

        // when
        var result = paymentMethodRepository.getByUserId(userId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(PaymentMethod::getProviderPaymentMethodId)
                .containsExactlyInAnyOrder("pm_1", "pm_2");
    }

    @Test
    void clearDefaultForUser_shouldClearDefaultPaymentMethod() {

        // given
        UUID userId = UUID.randomUUID();

        PaymentMethod paymentMethod = createPaymentMethod(
                userId,
                "pm_default",
                true
        );

        paymentMethodRepository.saveAndFlush(paymentMethod);

        // when
        paymentMethodRepository.clearDefaultForUser(userId);

        // then
        var result = paymentMethodRepository.findById(paymentMethod.getId());

        assertThat(result).isPresent();
        assertThat(result.get().isDefault()).isFalse();
    }

    @Test
    void findByIdAndUserId_shouldReturnPaymentMethod_whenIdAndUserMatch() {

        // given
        UUID userId = UUID.randomUUID();

        PaymentMethod paymentMethod = createPaymentMethod(
                userId,
                "pm_test",
                true
        );

        paymentMethodRepository.saveAndFlush(paymentMethod);

        // when
        var result = paymentMethodRepository.findByIdAndUserId(
                paymentMethod.getId(),
                userId
        );

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(paymentMethod.getId());
        assertThat(result.get().getUserId()).isEqualTo(userId);
    }

    @Test
    void existsDefaultByUserId_shouldReturnTrue_whenUserHasDefaultPaymentMethod() {

        // given
        UUID userId = UUID.randomUUID();

        PaymentMethod paymentMethod = createPaymentMethod(
                userId,
                "pm_default",
                true
        );

        paymentMethodRepository.saveAndFlush(paymentMethod);

        // when
        var result = paymentMethodRepository.existsDefaultByUserId(userId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void existsByUserIdAndCardFingerPrint_shouldReturnTrue_whenFingerprintExists() {

        // given
        UUID userId = UUID.randomUUID();

        PaymentMethod paymentMethod = createPaymentMethod(
                userId,
                "pm_test",
                false
        );

        paymentMethodRepository.saveAndFlush(paymentMethod);

        // when
        var result = paymentMethodRepository.existsByUserIdAndCardFingerPrint(
                userId,
                paymentMethod.getCardFingerPrint()
        );

        // then
        assertThat(result).isTrue();
    }

    @Test
    void deleteByIdAndUserId_shouldDeletePaymentMethod_whenIdAndUserMatch() {

        // given
        UUID userId = UUID.randomUUID();

        PaymentMethod paymentMethod = createPaymentMethod(
                userId,
                "pm_delete",
                false
        );

        paymentMethodRepository.saveAndFlush(paymentMethod);

        // when
        paymentMethodRepository.deleteByIdAndUserId(
                paymentMethod.getId(),
                userId
        );

        // then
        assertThat(paymentMethodRepository.findById(paymentMethod.getId()))
                .isEmpty();
    }

    @Test
    void findStripePaymentMethodIdByIdAndUserId_shouldReturnProviderPaymentMethodId_whenIdAndUserMatch() {

        // given
        UUID userId = UUID.randomUUID();

        PaymentMethod paymentMethod = createPaymentMethod(
                userId,
                "pm_stripe_123",
                true
        );

        paymentMethodRepository.saveAndFlush(paymentMethod);

        // when
        var result = paymentMethodRepository.findStripePaymentMethodIdByIdAndUserId(
                paymentMethod.getId(),
                userId
        );

        // then
        assertThat(result)
                .isPresent()
                .contains("pm_stripe_123");
    }

    private PaymentMethod createPaymentMethod(
            UUID userId,
            String providerPaymentMethodId,
            boolean isDefault
    ) {
        PaymentMethod paymentMethod = new PaymentMethod();

        paymentMethod.setId(UUID.randomUUID());
        paymentMethod.setUserId(userId);
        paymentMethod.setProvider(PaymentProvider.STRIPE);
        paymentMethod.setProviderPaymentMethodId(providerPaymentMethodId);
        paymentMethod.setCardFingerPrint("fingerprint-" + providerPaymentMethodId);
        paymentMethod.setBrand("VISA");
        paymentMethod.setLast4("4242");
        paymentMethod.setExpMonth(12);
        paymentMethod.setExpYear(2030);
        paymentMethod.setDefault(isDefault);

        return paymentMethod;
    }
}
