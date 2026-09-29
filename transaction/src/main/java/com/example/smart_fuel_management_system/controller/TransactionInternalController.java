package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.*;
import com.example.smart_fuel_management_system.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/transactions")
@RequiredArgsConstructor
public class TransactionInternalController {

    private final TransactionService transactionService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<TransactionCountResponse> getUserTransactionCount(@PathVariable String userId){
        UUID id = UUID.fromString(userId);
        return ResponseEntity.ok(transactionService.getUserTransactionCount(id));
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<TransactionDashboardSummaryResponse> getDashboardSummary(){
        return ResponseEntity.ok(transactionService.getDashboardSummary());
    }

    @GetMapping("/stations/{stationId}")
    public ResponseEntity<TransactionStationResponse> getStationTransactions(@PathVariable String stationId){
        UUID id = UUID.fromString(stationId);
        return ResponseEntity.ok(transactionService.getTransactionsCountByStationId(id));
    }

    @PostMapping("/stations/by-ids")
    public ResponseEntity<List<TransactionStationResponse>> getStationsTransactions(
            @RequestBody List<UUID> stationIds
    ) {
        return ResponseEntity.ok(
                transactionService.getStationsTransactions(stationIds)
        );
    }
}
