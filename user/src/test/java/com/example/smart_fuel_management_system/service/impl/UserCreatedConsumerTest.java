package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class UserCreatedConsumerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserCreatedConsumer consumer;

    @Test
    void consume_shouldDelegateToUserService_whenPayloadIsValid() throws Exception {
        // given
        String payload = "{\"id\":\"123\"}";

        // when
        consumer.consume(payload);

        // then
        verify(userService).create(payload);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void consume_shouldPropagateException_whenUserServiceFails() throws Exception {
        // given
        String payload = "not-json";
        JsonProcessingException failure = mock(JsonProcessingException.class);
        doThrow(failure).when(userService).create(payload);

        // when / then
        assertThatThrownBy(() -> consumer.consume(payload))
                .isSameAs(failure);

        verify(userService).create(payload);
    }
}