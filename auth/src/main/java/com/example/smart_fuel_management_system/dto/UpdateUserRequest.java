package com.example.smart_fuel_management_system.dto;

import com.example.smart_fuel_management_system.enums.AccountStatusType;

public record UpdateUserRequest(AccountStatusType accountStatusType) {
}
