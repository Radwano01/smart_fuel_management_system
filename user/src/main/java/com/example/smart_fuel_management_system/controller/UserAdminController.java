package com.example.smart_fuel_management_system.controller;


import com.example.smart_fuel_management_system.dto.UpdateDTO;
import com.example.smart_fuel_management_system.dto.UserSummaryResponse;
import com.example.smart_fuel_management_system.enums.AccountSearchType;
import com.example.smart_fuel_management_system.enums.AccountStatusType;
import com.example.smart_fuel_management_system.enums.RoleType;
import com.example.smart_fuel_management_system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/users")
public class UserAdminController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserSummaryResponse>> getUsers(
            @RequestParam(required = false) AccountSearchType searchType,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AccountStatusType status,
            Pageable pageable) {

        return ResponseEntity.ok(
                userService.getUsersBySearch(
                        searchType,
                        search,
                        status,
                        pageable
                )
        );
    }

    @PatchMapping("/{employeeId}")
    public ResponseEntity<Void> updateUser(
            Authentication authentication,
            @RequestBody UpdateDTO request) {
        UUID userId = UUID.fromString(authentication.getName());
        userService.updateUser(userId, request);
        return ResponseEntity.noContent().build();
    }
}
