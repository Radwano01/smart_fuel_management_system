package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.UserDashboardSummaryResponse;
import com.example.smart_fuel_management_system.dto.UserResponseToPaymentService;
import com.example.smart_fuel_management_system.service.UserService;
import com.example.smart_fuel_management_system.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/users")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserResponse(@PathVariable String id) {
        UUID userId = UUID.fromString(id);
        return ResponseEntity.ok(userService.getUserResponse(userId));
    }

    @GetMapping("/{id}/payment")
    public ResponseEntity<UserResponseToPaymentService> getUserForPayment(
            @PathVariable UUID id
    ) {

        UserResponseToPaymentService response =
                userService.getUserForPayment(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<UserDashboardSummaryResponse> getDashboardSummary(){
        return ResponseEntity.ok(userService.getDashboardSummary());
    }

    @PatchMapping("/{id}/email")
    public ResponseEntity<Void> updateEmail(@PathVariable String id, @RequestBody String email){
        UUID userId = UUID.fromString(id);
        userService.updateEmail(userId, email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/by-ids")
    public ResponseEntity<List<UserResponse>> getUsersByIds(
            @RequestBody List<UUID> ids) {

        return ResponseEntity.ok(
                userService.getUsersByIds(ids)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> getUsersByFullName(
            @RequestParam("fullName") String fullName
    ) {

        return ResponseEntity.ok(
                userService.getUsersByFullName(fullName)
        );
    }
}
