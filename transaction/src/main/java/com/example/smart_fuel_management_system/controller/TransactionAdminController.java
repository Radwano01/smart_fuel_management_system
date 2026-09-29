package com.example.smart_fuel_management_system.controller;

import com.example.smart_fuel_management_system.dto.TransactionResponse;
import com.example.smart_fuel_management_system.dto.TransactionStatisticsResponse;
import com.example.smart_fuel_management_system.dto.TransactionSummaryResponse;
import com.example.smart_fuel_management_system.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/transactions")
@RequiredArgsConstructor
public class TransactionAdminController {

    private final TransactionService transactionService;

    @GetMapping("/statistics")
    public ResponseEntity<TransactionStatisticsResponse> getStatistics(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(required = false) List<UUID> stationIds
    ) {
        return ResponseEntity.ok(
                transactionService.getStatistics(
                        from,
                        to,
                        stationIds
                )
        );
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable UUID transactionId
    ) {
        return ResponseEntity.ok(
                transactionService.getTransaction(transactionId)
        );
    }


    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<TransactionResponse>> getUserTransactions(@PathVariable String userId, Pageable pageable){
        UUID id = UUID.fromString(userId);
        return ResponseEntity.ok(transactionService.getUserTransactions(id, pageable));
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                transactionService.getTransactions(from, to, pageable)
        );
    }

    @GetMapping("/vehicles/{vehicleId}")
    public ResponseEntity<List<TransactionSummaryResponse>> getVehicleTransactionHistory(@PathVariable String vehicleId){
        UUID id = UUID.fromString(vehicleId);
        return ResponseEntity.ok(transactionService.getVehicleTransactionHistory(id));
    }
}
