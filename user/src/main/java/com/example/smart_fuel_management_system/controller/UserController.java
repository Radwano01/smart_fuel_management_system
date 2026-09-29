package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.service.UserService;
import com.example.smart_fuel_management_system.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserDTO> getUser(
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PatchMapping
    public ResponseEntity<Void> updateUser(
            Authentication authentication,
            @RequestBody UpdateDTO request) {
        UUID userId = UUID.fromString(authentication.getName());
        userService.updateUser(userId, request);
        return ResponseEntity.noContent().build();
    }
}