package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.*;
import com.example.smart_fuel_management_system.enums.AccountSearchType;
import com.example.smart_fuel_management_system.enums.AccountStatusType;
import com.example.smart_fuel_management_system.service.EmployeeService;
import com.example.smart_fuel_management_system.service.IdentifierChangeService;
import com.example.smart_fuel_management_system.service.RegisterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/auth")
public class AuthAdminController {

    private final RegisterService registerService;
    private final EmployeeService employeeService;
    private final IdentifierChangeService identifierChangeService;

    @PostMapping("/station-accounts")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterStationRequest request) throws JsonProcessingException {
        registerService.registerStationEmployee(request);
        return ResponseEntity.status(201).build();
    }


    @GetMapping("/employees/{employeeId}/details")
    public ResponseEntity<EmployeeDetailsResponse> getEmployeeDetails(@PathVariable String employeeId){
        UUID id = UUID.fromString(employeeId);
        return ResponseEntity.ok(employeeService.getEmployeeDetails(id));
    }

    @PatchMapping("/station-accounts/{employeeId}")
    public ResponseEntity<Void> updateStationAccount(@PathVariable String employeeId,
                                       @RequestBody UpdateEmployeeRequest request) {
        UUID id = UUID.fromString(employeeId);
        employeeService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Void> updateUserAccount(@PathVariable String userId,
                                                  @RequestBody UpdateUserRequest request) {
        UUID id = UUID.fromString(userId);
        employeeService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{authId}/identifier-change")
    public ResponseEntity<UUID> changeIdentifier(
            @PathVariable String authId,
            @RequestBody UpdateIdentifierRequest request) {

        UUID id = UUID.fromString(authId);
        UUID changeId = identifierChangeService.change(id, request);

        return ResponseEntity.ok(changeId);
    }

    @PostMapping("/identifier-change/{changeId}/verify")
    public ResponseEntity<Void> verifyOtp(
            @PathVariable String changeId,
            @RequestBody VerifyIdentifierChangeOtpRequest request) {
        UUID id = UUID.fromString(changeId);
        identifierChangeService.verifyOtp(
                id,
                request.otp()
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/identifier-change/{changeId}/resend")
    public ResponseEntity<Void> resendOtp(
            @PathVariable String changeId) {
        UUID id = UUID.fromString(changeId);
        identifierChangeService.resendOtp(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/station-accounts")
    public ResponseEntity<Page<EmployeeResponse>> getEmployees(
            @RequestParam(required = false) AccountSearchType searchType,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AccountStatusType status,
            Pageable pageable) {

        return ResponseEntity.ok(
                employeeService.search(
                        searchType,
                        search,
                        status,
                        pageable
                )
        );
    }
}
