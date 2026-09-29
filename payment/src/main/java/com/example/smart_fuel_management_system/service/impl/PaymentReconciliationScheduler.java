package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.entity.Payment;
import com.example.smart_fuel_management_system.enums.PaymentStatusType;
import com.example.smart_fuel_management_system.repository.PaymentRepository;
import com.example.smart_fuel_management_system.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentReconciliationScheduler {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void reconcilePayments() {

        List<Payment> payments =
                paymentRepository.findByStatus(PaymentStatusType.PROCESSING);

        for (Payment payment : payments) {
            try {
                paymentService.reconcile(payment);
            } catch (Exception e) {
                log.error("Failed to reconcile payment {}", payment.getId(), e);
            }
        }
    }
}