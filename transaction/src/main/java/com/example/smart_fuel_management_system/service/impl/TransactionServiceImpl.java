package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.*;
import com.example.smart_fuel_management_system.entity.Transaction;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.enums.PaymentStatusType;
import com.example.smart_fuel_management_system.repository.TransactionRepository;
import com.example.smart_fuel_management_system.service.TransactionService;
import com.example.smart_fuel_management_system.service.impl.client.StationClient;
import com.example.smart_fuel_management_system.service.impl.client.VehicleClient;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final VehicleClient vehicleClient;
    private final StationClient stationClient;

    @Override
    public void createFromPayment(PaymentEvent event) {

        boolean exists = transactionRepository.existsByPaymentId(event.paymentId());
        if (exists) return;

        if (event.status() != PaymentStatusType.SUCCESS) {
            return;
        }

        Transaction tx = Transaction.builder()
                .paymentId(event.paymentId())
                .userId(event.userId())
                .vehicleId(event.vehicleId())
                .fuelSessionId(event.fuelSessionId())
                .stationId(event.stationId())
                .pumpId(event.pumpId())
                .fuelType(event.fuelType())
                .liters(event.liters())
                .pricePerLiter(event.pricePerLiter())
                .amount(event.amount())
                .currency(event.currency())
                .status(event.status())
                .build();

        transactionRepository.save(tx);
    }

    @Transactional(readOnly = true)
    @Override
    public TransactionResponse getTransaction(UUID transactionId) {

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() ->new EntityNotFoundException(
                                "Transaction not found: " + transactionId)
                );

        VehicleTransactionResponse vehicle = vehicleClient.getVehicleDetails(transaction.getVehicleId()).orElse(null);

        StationTransactionResponse station = stationClient.getStationDetails(transaction.getStationId()).orElse(null);

        return new TransactionResponse(
                transaction.getId(),
                transaction.getPumpId(),
                transaction.getFuelType(),
                transaction.getLiters(),
                transaction.getPricePerLiter(),
                transaction.getAmount(),
                transaction.getCreatedAt(),
                transaction.getStatus(),
                vehicle,
                station
        );
    }


    @Transactional(readOnly = true)
    @Override
    public Page<TransactionResponse> getTransactions(
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    ) {

        Page<Transaction> transactions;

        if (from == null && to == null) {
            transactions = transactionRepository
                    .findAllByOrderByCreatedAtDesc(pageable);
        } else {
            transactions = transactionRepository
                    .findByDateRange(from, to, pageable);
        }

        return mapToTransactionResponses(transactions);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TransactionSummaryResponse> getByUserId(UUID userId) {

        return transactionRepository.findByUserId(userId)
                .stream()
                .map(tx -> TransactionSummaryResponse.builder()
                        .id(tx.getId())
                        .amount(tx.getAmount())
                        .fuelType(tx.getFuelType())
                        .createdAt(tx.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public TransactionDetailsResponse getById(UUID id, UUID userId) {

        Transaction tx = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        VehicleTransactionResponse vehicle = vehicleClient.getVehicleDetails(tx.getVehicleId()).orElse(null);

        StationTransactionResponse station = stationClient.getStationDetails(tx.getStationId()).orElse(null);

        return TransactionDetailsResponse.builder()
                .id(tx.getId())
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .liters(tx.getLiters())
                .pricePerLiter(tx.getPricePerLiter())
                .fuelType(tx.getFuelType())
                .status(tx.getStatus())
                .createdAt(tx.getCreatedAt())
                .vehicle(vehicle)
                .station(station)
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public TransactionCountResponse getUserTransactionCount(UUID userId){
        return new TransactionCountResponse(transactionRepository.countByUserId(userId));
    }

    @Transactional(readOnly = true)
    @Override
    public TransactionStationResponse getTransactionsCountByStationId(UUID stationId) {

        return new TransactionStationResponse(
                stationId,
                transactionRepository.countByStationId(stationId),
                transactionRepository.countVehiclesByStationId(stationId)
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<TransactionSummaryResponse> getVehicleTransactionHistory(UUID vehicleId) {
        return transactionRepository.findByVehicleId(vehicleId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TransactionStationResponse> getStationsTransactions(
            List<UUID> stationIds
    ) {

        if (stationIds == null || stationIds.isEmpty()) {
            return List.of();
        }

        return transactionRepository
                .getStationTransactionStatistics(stationIds);
    }

    @Override
    public List<TransactionSummaryResponse> recentTransactions(UUID userId) {
        return transactionRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(tx -> new TransactionSummaryResponse(
                        tx.getId(),
                        tx.getAmount(),
                        tx.getFuelType(),
                        tx.getCreatedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<TransactionResponse> getUserTransactions(
            UUID userId,
            Pageable pageable) {

        Page<Transaction> transactions =
                transactionRepository.findByUserId(userId, pageable);

        List<UUID> vehicleIds = transactions.getContent().stream()
                .map(Transaction::getVehicleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<UUID> stationIds = transactions.getContent().stream()
                .map(Transaction::getStationId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UUID, VehicleTransactionResponse> vehicles =
                vehicleClient.getVehiclesDetails(vehicleIds).stream()
                        .collect(Collectors.toMap(
                                VehicleTransactionResponse::id,
                                Function.identity()
                        ));

        Map<UUID, StationTransactionResponse> stations =
                stationClient.getStationsDetails(stationIds).stream()
                        .collect(Collectors.toMap(
                                StationTransactionResponse::id,
                                Function.identity()
                        ));

        return transactions.map(transaction ->
                new TransactionResponse(
                        transaction.getId(),
                        transaction.getPumpId(),
                        transaction.getFuelType(),
                        transaction.getLiters(),
                        transaction.getPricePerLiter(),
                        transaction.getAmount(),
                        transaction.getCreatedAt(),
                        transaction.getStatus(),
                        vehicles.get(transaction.getVehicleId()),
                        stations.get(transaction.getStationId())
                )
        );
    }

    @Transactional(readOnly = true)
    @Override
    public TransactionDashboardSummaryResponse getDashboardSummary() {

        LocalDate today = LocalDate.now();

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        long transactionsCount = transactionRepository.count();

        long todayTransactions =
                transactionRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        start, end);

        BigDecimal todayFuelVolume =
                transactionRepository.sumLitersBetween(start, end);

        BigDecimal todayRevenue =
                transactionRepository.sumAmountBetween(start, end);

        List<FuelTypeSalesResponse> fuelSales =
                transactionRepository.getFuelSalesByType(start, end);

        List<TransactionSummaryResponse> recentTransactions =
                getRecentTransactions();

        return new TransactionDashboardSummaryResponse(
                transactionsCount,
                todayTransactions,
                todayFuelVolume,
                todayRevenue,
                fuelSales,
                recentTransactions
        );
    }

    @Transactional(readOnly = true)
    @Override
    public TransactionStatisticsResponse getStatistics(
            LocalDate from,
            LocalDate to,
            List<UUID> stationIds
    ) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        long transactionCount =
                transactionRepository
                        .countByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndStationIdIn(
                                start,
                                end,
                                stationIds
                        );

        BigDecimal fuelVolume =
                transactionRepository.sumLiters(
                        start,
                        end,
                        stationIds
                );

        BigDecimal revenue =
                transactionRepository.sumRevenue(
                        start,
                        end,
                        stationIds
                );

        List<FuelTypeSalesResponse> fuelSalesByType =
                transactionRepository.getFuelSalesByType(
                                start,
                                end,
                                stationIds
                        )
                        .stream()
                        .map(row -> new FuelTypeSalesResponse(
                                (FuelType) row[0],
                                (BigDecimal) row[1],
                                (BigDecimal) row[2]
                        ))
                        .toList();

        return new TransactionStatisticsResponse(
                transactionCount,
                fuelVolume,
                revenue,
                fuelSalesByType
        );
    }

    private List<TransactionSummaryResponse> getRecentTransactions() {
        return transactionRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(tx -> new TransactionSummaryResponse(
                        tx.getId(),
                        tx.getAmount(),
                        tx.getFuelType(),
                        tx.getCreatedAt()
                ))
                .toList();
    }

    private Page<TransactionResponse> mapToTransactionResponses(
            Page<Transaction> transactions
    ) {

        List<UUID> vehicleIds = transactions.getContent()
                .stream()
                .map(Transaction::getVehicleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<UUID> stationIds = transactions.getContent()
                .stream()
                .map(Transaction::getStationId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UUID, VehicleTransactionResponse> vehicleMap =
                vehicleClient.getVehiclesDetails(vehicleIds)
                        .stream()
                        .collect(Collectors.toMap(
                                VehicleTransactionResponse::id,
                                Function.identity()
                        ));

        Map<UUID, StationTransactionResponse> stationMap =
                stationClient.getStationsDetails(stationIds)
                        .stream()
                        .collect(Collectors.toMap(
                                StationTransactionResponse::id,
                                Function.identity()
                        ));

        return transactions.map(transaction ->
                new TransactionResponse(
                        transaction.getId(),
                        transaction.getPumpId(),
                        transaction.getFuelType(),
                        transaction.getLiters(),
                        transaction.getPricePerLiter(),
                        transaction.getAmount(),
                        transaction.getCreatedAt(),
                        transaction.getStatus(),
                        vehicleMap.get(transaction.getVehicleId()),
                        stationMap.get(transaction.getStationId())
                )
        );
    }
}