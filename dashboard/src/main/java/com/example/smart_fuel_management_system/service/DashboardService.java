package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.DashboardAdminResponse;
import com.example.smart_fuel_management_system.dto.DashboardUserResponse;

import java.util.UUID;

public interface DashboardService {
    DashboardAdminResponse getDashboard();
    DashboardUserResponse getDashboard(UUID userId);
}
