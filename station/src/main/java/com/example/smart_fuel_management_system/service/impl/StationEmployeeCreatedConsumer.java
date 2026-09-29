package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.service.StationEmployeeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StationEmployeeCreatedConsumer {

    private final StationEmployeeService stationEmployeeService;

    @RabbitListener(queues = "station.employee.created.queue")
    public void consume(String payload) throws JsonProcessingException {
        stationEmployeeService.create(payload);
    }
}
