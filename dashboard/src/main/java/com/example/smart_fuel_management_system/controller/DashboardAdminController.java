package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.DashboardAdminResponse;
import com.example.smart_fuel_management_system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/dashboard")
public class DashboardAdminController {

    private final DashboardService service;

    @GetMapping
    public ResponseEntity<DashboardAdminResponse> getDashboard() {
        return ResponseEntity.ok(
                service.getDashboard()
        );
    }
}
