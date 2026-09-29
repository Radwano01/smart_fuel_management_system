package com.example.smart_fuel_management_system.dto.station;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;


public record CreateStationRequest(

        @NotNull
        String name,
        @NotNull
        String city,
        @NotNull
        String address,
        @NotNull
        String contactInformation,
        @NotNull
        BigDecimal latitude,
        @NotNull
        BigDecimal longitude
) {}