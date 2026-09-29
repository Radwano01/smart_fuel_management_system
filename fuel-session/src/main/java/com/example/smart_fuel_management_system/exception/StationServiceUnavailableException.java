package com.example.smart_fuel_management_system.exception;

public class StationServiceUnavailableException extends RuntimeException {

    public StationServiceUnavailableException(String message) {
        super(message);
    }

    public StationServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}