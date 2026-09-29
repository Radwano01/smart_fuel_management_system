package com.example.smart_fuel_management_system.exception;

public class MqttConnectionException extends RuntimeException {
    public MqttConnectionException(String message) {
        super(message);
    }

    public MqttConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
