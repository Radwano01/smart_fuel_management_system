package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.service.StationEmployeeService;
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
class StationEmployeeCreatedConsumerTest {

    @Mock
    private StationEmployeeService stationEmployeeService;

    @InjectMocks
    private StationEmployeeCreatedConsumer consumer;

    @Test
    void consume_shouldDelegateToStationEmployeeService_whenPayloadIsValid() throws Exception {
        // given
        String payload = "{\"employeeId\":\"123\"}";

        // when
        consumer.consume(payload);

        // then
        verify(stationEmployeeService).create(payload);
        verifyNoMoreInteractions(stationEmployeeService);
    }

    @Test
    void consume_shouldPropagateException_whenServiceFails() throws Exception {
        // given
        String payload = "not-json";
        JsonProcessingException failure = mock(JsonProcessingException.class);
        doThrow(failure).when(stationEmployeeService).create(payload);

        // when / then
        assertThatThrownBy(() -> consumer.consume(payload))
                .isSameAs(failure);

        verify(stationEmployeeService).create(payload);
    }
}