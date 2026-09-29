package com.example.smart_fuel_management_system.controller;
import com.example.smart_fuel_management_system.dto.TransactionDetailsResponse;
import com.example.smart_fuel_management_system.dto.TransactionSummaryResponse;
import com.example.smart_fuel_management_system.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("{id}")
    public ResponseEntity<TransactionDetailsResponse> getById(@PathVariable String id, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        UUID transactionId = UUID.fromString(id);
        return ResponseEntity.ok(transactionService.getById(transactionId, userId));
    }

    @GetMapping
    public ResponseEntity<List<TransactionSummaryResponse>> getByUser(Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        return ResponseEntity.ok(transactionService.getByUserId(userId));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<TransactionSummaryResponse>> getRecentTransactions(Principal principal){
        UUID userId = UUID.fromString(principal.getName());
        return ResponseEntity.ok(transactionService.recentTransactions(userId));
    }
}
