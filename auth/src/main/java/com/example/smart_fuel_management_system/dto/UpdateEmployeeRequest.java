package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.AccountStatusType;

public record UpdateEmployeeRequest(
        String password,
        AccountStatusType accountStatusType
) {
}
