package com.example.smart_fuel_management_system.controller;


import com.example.smart_fuel_management_system.dto.AuthUserResponse;
import com.example.smart_fuel_management_system.dto.StationEmployeeResponse;
import com.example.smart_fuel_management_system.enums.AccountStatusType;
import com.example.smart_fuel_management_system.enums.RoleType;
import com.example.smart_fuel_management_system.service.EmployeeService;
import com.example.smart_fuel_management_system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/internal/auth")
public class AuthInternalController {

    private final EmployeeService employeeService;
    private final UserService userService;

    @GetMapping("/stations/{employeeId}/employee")
    public ResponseEntity<StationEmployeeResponse> getEmployeeInfo(
            @PathVariable String employeeId
    ) {
        UUID id = UUID.fromString(employeeId);
        StationEmployeeResponse response =
                employeeService.getEmployeeInfo(id);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/by-ids")
    public ResponseEntity<List<AuthUserResponse>> getUsersByIds(
            @RequestBody List<UUID> userIds) {

        return ResponseEntity.ok(
                userService.getUsersByIds(userIds)
        );
    }

    @GetMapping("/search/phone")
    public ResponseEntity<List<AuthUserResponse>> searchByPhone(
            @RequestParam String search,
            Pageable pageable) {

        return ResponseEntity.ok(
                userService.searchByPhone(search, pageable)
        );
    }

    @GetMapping("/filter/status")
    public ResponseEntity<List<AuthUserResponse>> getUsersByStatus(
            @RequestParam AccountStatusType status,
            Pageable pageable) {

        return ResponseEntity.ok(
                userService.getUsersByStatus(status, pageable)
        );
    }
}
