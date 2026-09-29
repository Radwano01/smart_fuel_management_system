package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.entity.Payment;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.enums.PaymentStatusType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void findByPaymentIntentId_shouldReturnPayment_whenPaymentIntentIdExists() {

        // given
        Payment payment = createPayment(
                "pi_test_123",
                PaymentStatusType.SUCCESS
        );

        paymentRepository.saveAndFlush(payment);

        // when
        var result = paymentRepository.findByPaymentIntentId("pi_test_123");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getPaymentIntentId())
                .isEqualTo("pi_test_123");
    }

    @Test
    void findByStatus_shouldReturnPayments_withMatchingStatus() {

        // given
        Payment successfulPayment = createPayment(
                "pi_success",
                PaymentStatusType.SUCCESS
        );

        Payment processingPayment = createPayment(
                "pi_processing",
                PaymentStatusType.PROCESSING
        );

        paymentRepository.save(successfulPayment);
        paymentRepository.saveAndFlush(processingPayment);

        // when
        var result = paymentRepository.findByStatus(PaymentStatusType.SUCCESS);

        // then
        assertThat(result)
                .hasSize(1)
                .containsExactly(successfulPayment);
    }

    private Payment createPayment(
            String paymentIntentId,
            PaymentStatusType status
    ) {
        Payment payment = new Payment();

        payment.setUserId(UUID.randomUUID());
        payment.setVehicleId(UUID.randomUUID());
        payment.setStationId(UUID.randomUUID());
        payment.setPumpId(UUID.randomUUID());
        payment.setFuelSessionId(UUID.randomUUID());
        payment.setPaymentIntentId(paymentIntentId);
        payment.setPaymentMethodId("pm_test");
        payment.setFuelType(FuelType.GASOLINE);
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setEstimatedLiters(BigDecimal.valueOf(20));
        payment.setPricePerLiter(BigDecimal.valueOf(5));
        payment.setCurrency("TRY");
        payment.setStatus(status);

        return payment;
    }
}