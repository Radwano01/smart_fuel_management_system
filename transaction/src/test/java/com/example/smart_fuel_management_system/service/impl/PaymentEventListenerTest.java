package com.example.smart_fuel_management_system.listener;

import com.example.smart_fuel_management_system.dto.PaymentEvent;
import com.example.smart_fuel_management_system.service.TransactionService;
import com.example.smart_fuel_management_system.service.impl.PaymentEventListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PaymentEventListenerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private PaymentEventListener paymentEventListener;

    @Test
    void consume_delegatesEventToService() {
        // given
        PaymentEvent event = mock(PaymentEvent.class);

        // when
        paymentEventListener.consume(event);

        // then
        verify(transactionService, times(1)).createFromPayment(event);
        verifyNoMoreInteractions(transactionService);
    }

    @Test
    void consume_propagatesServiceException() {
        // given
        PaymentEvent event = mock(PaymentEvent.class);
        RuntimeException failure = new RuntimeException("service failure");
        org.mockito.Mockito.doThrow(failure)
                .when(transactionService).createFromPayment(event);

        // when / then
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> paymentEventListener.consume(event))
                .isSameAs(failure);

        verify(transactionService, times(1)).createFromPayment(event);
    }
}