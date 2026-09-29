package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.entity.PaymentCustomer;
import com.example.smart_fuel_management_system.enums.PaymentProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PaymentCustomerRepositoryTest {

    @Autowired
    private PaymentCustomerRepository paymentCustomerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void findPreAuthData_shouldReturnCustomerAndPaymentMethod_whenDefaultPaymentMethodExists() {

        // given
        UUID userId = UUID.randomUUID();

        PaymentCustomer customer = new PaymentCustomer();
        customer.setUserId(userId);
        customer.setProvider(PaymentProvider.STRIPE);
        customer.setProviderCustomerId("cus_test");

        paymentCustomerRepository.saveAndFlush(customer);

        UUID paymentMethodId = UUID.randomUUID();

        jdbcTemplate.update("""
            INSERT INTO payment_method (
                id,
                user_id,
                provider,
                provider_payment_method_id,
                card_finger_print,
                is_default
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """,
                paymentMethodId,
                userId,
                PaymentProvider.STRIPE.name(),
                "pm_test",
                "fingerprint_test",
                true
        );

        // when
        var result = paymentCustomerRepository.findPreAuthData(
                userId,
                PaymentProvider.STRIPE
        );

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getCustomerId()).isEqualTo("cus_test");
        assertThat(result.get().getPaymentMethodId()).isEqualTo("pm_test");
    }

    @Test
    void findPreAuthData_shouldReturnEmpty_whenNoDefaultPaymentMethodExists() {

        // given
        UUID userId = UUID.randomUUID();

        PaymentCustomer customer = new PaymentCustomer();
        customer.setUserId(userId);
        customer.setProvider(PaymentProvider.STRIPE);
        customer.setProviderCustomerId("cus_test");

        paymentCustomerRepository.saveAndFlush(customer);

        UUID paymentMethodId = UUID.randomUUID();


        // when
        var result = paymentCustomerRepository.findPreAuthData(
                userId,
                PaymentProvider.STRIPE
        );

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findPreAuthData_shouldReturnEmpty_whenPaymentProviderDoesNotMatch() {

        // given
        UUID userId = UUID.randomUUID();

        PaymentCustomer customer = new PaymentCustomer();
        customer.setUserId(userId);
        customer.setProvider(PaymentProvider.STRIPE);
        customer.setProviderCustomerId("cus_test");

        paymentCustomerRepository.saveAndFlush(customer);

        UUID paymentMethodId = UUID.randomUUID();



        // when
        var result = paymentCustomerRepository.findPreAuthData(
                userId,
                PaymentProvider.PAYPAL
        );

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByUserIdAndProvider_shouldReturnPaymentCustomer_whenExists() {

        // given
        UUID userId = UUID.randomUUID();

        PaymentCustomer customer = new PaymentCustomer();
        customer.setUserId(userId);
        customer.setProvider(PaymentProvider.STRIPE);
        customer.setProviderCustomerId("cus_test");

        paymentCustomerRepository.saveAndFlush(customer);

        // when
        var result = paymentCustomerRepository.findByUserIdAndProvider(
                userId,
                PaymentProvider.STRIPE
        );

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(userId);
        assertThat(result.get().getProvider()).isEqualTo(PaymentProvider.STRIPE);
        assertThat(result.get().getProviderCustomerId()).isEqualTo("cus_test");
    }

    @Test
    void findByUserIdAndProvider_shouldReturnEmpty_whenNotExists() {

        // given
        UUID userId = UUID.randomUUID();

        // when
        var result = paymentCustomerRepository.findByUserIdAndProvider(
                userId,
                PaymentProvider.STRIPE
        );

        // then
        assertThat(result).isEmpty();
    }
}