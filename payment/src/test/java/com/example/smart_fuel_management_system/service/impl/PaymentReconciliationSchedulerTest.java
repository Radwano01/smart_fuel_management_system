package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.entity.Payment;
import com.example.smart_fuel_management_system.enums.PaymentStatusType;
import com.example.smart_fuel_management_system.repository.PaymentRepository;
import com.example.smart_fuel_management_system.service.PaymentService;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentReconciliationSchedulerTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentReconciliationScheduler paymentReconciliationScheduler;

    @Test
    void reconcilePayments_shouldReconcileProcessingPayments() throws StripeException {
        // given
        Payment firstPayment = new Payment();
        Payment secondPayment = new Payment();

        when(paymentRepository.findByStatus(PaymentStatusType.PROCESSING))
                .thenReturn(List.of(firstPayment, secondPayment));

        // when
        paymentReconciliationScheduler.reconcilePayments();

        // then
        verify(paymentService).reconcile(firstPayment);
        verify(paymentService).reconcile(secondPayment);
    }

    @Test
    void reconcilePayments_shouldDoNothing_whenNoProcessingPayments() {
        // given
        when(paymentRepository.findByStatus(PaymentStatusType.PROCESSING))
                .thenReturn(List.of());

        // when
        paymentReconciliationScheduler.reconcilePayments();

        // then
        verify(paymentRepository).findByStatus(PaymentStatusType.PROCESSING);
    }

    @Test
    void reconcilePayments_shouldContinueWithNextPayment_whenReconciliationFails() throws StripeException {
        // given
        Payment firstPayment = new Payment();
        Payment secondPayment = new Payment();

        when(paymentRepository.findByStatus(PaymentStatusType.PROCESSING))
                .thenReturn(List.of(firstPayment, secondPayment));

        org.mockito.Mockito.doThrow(new RuntimeException("Reconciliation failed"))
                .when(paymentService)
                .reconcile(firstPayment);

        // when
        paymentReconciliationScheduler.reconcilePayments();

        // then
        verify(paymentService).reconcile(firstPayment);
        verify(paymentService).reconcile(secondPayment);
    }
}