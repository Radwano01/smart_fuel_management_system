package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.PaymentEvent;
import com.example.smart_fuel_management_system.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final TransactionService transactionService;

    @RabbitListener(queues = "transaction.created.queue")
    public void consume(PaymentEvent event) {
        transactionService.createFromPayment(event);
    }
}