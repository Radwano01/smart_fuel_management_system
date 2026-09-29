package com.example.smart_fuel_management_system.exception;

public class MqttMessageProcessingException extends RuntimeException {
    public MqttMessageProcessingException(String message) {
        super(message);
    }

    public MqttMessageProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
