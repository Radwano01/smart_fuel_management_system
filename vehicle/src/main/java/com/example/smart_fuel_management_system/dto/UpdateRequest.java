package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.FuelType;

public record UpdateRequest(String brand,
                            String model,
                            FuelType fuelType) {}
