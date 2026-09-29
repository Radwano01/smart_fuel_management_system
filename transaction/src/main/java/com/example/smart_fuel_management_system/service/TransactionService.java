package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionService {
    void createFromPayment(PaymentEvent event);
    TransactionResponse getTransaction(UUID transactionId);
    Page<TransactionResponse> getTransactions(
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );
    List<TransactionSummaryResponse> getByUserId(UUID userId);
    TransactionDetailsResponse getById(UUID id, UUID transactionId);
    TransactionCountResponse getUserTransactionCount(UUID userId);
    Page<TransactionResponse> getUserTransactions(
            UUID userId,
            Pageable pageable);
    TransactionDashboardSummaryResponse getDashboardSummary();
    TransactionStatisticsResponse getStatistics(
            LocalDate from,
            LocalDate to,
            List<UUID> stationIds
    );
    TransactionStationResponse getTransactionsCountByStationId(UUID stationId);
    List<TransactionSummaryResponse> getVehicleTransactionHistory(UUID vehicleId);
    List<TransactionStationResponse> getStationsTransactions(List<UUID> stationIds);
    List<TransactionSummaryResponse> recentTransactions(UUID userId);
}
