package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.dto.FuelTypeSalesResponse;
import com.example.smart_fuel_management_system.dto.TransactionStationResponse;
import com.example.smart_fuel_management_system.dto.TransactionSummaryResponse;
import com.example.smart_fuel_management_system.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    boolean existsByPaymentId(UUID paymentId);

    List<Transaction> findByUserId(UUID userId);

    Page<Transaction> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByUserId(UUID userId);

    long countByStationId(UUID stationId);

    @Query("""
    SELECT COUNT(DISTINCT t.vehicleId)
    FROM Transaction t
    WHERE t.stationId = :stationId
""")
    long countVehiclesByStationId(@Param("stationId") UUID stationId);

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT COALESCE(SUM(t.liters), 0)
        FROM Transaction t
        WHERE t.createdAt >= :start
          AND t.createdAt < :end
        """)
    BigDecimal sumLitersBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.createdAt >= :start
          AND t.createdAt < :end
        """)
    BigDecimal sumAmountBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT new com.example.smart_fuel_management_system.dto.FuelTypeSalesResponse(
            t.fuelType,
            SUM(t.liters),
            SUM(t.amount)
        )
        FROM Transaction t
        WHERE t.createdAt >= :start
          AND t.createdAt < :end
        GROUP BY t.fuelType
        ORDER BY t.fuelType
        """)
    List<FuelTypeSalesResponse> getFuelSalesByType(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<Transaction> findTop10ByOrderByCreatedAtDesc();

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndStationIdIn(
            LocalDateTime start,
            LocalDateTime end,
            Collection<UUID> stationIds
    );

    @Query("""
        SELECT COALESCE(SUM(t.liters), 0)
        FROM Transaction t
        WHERE t.createdAt >= :start
          AND t.createdAt < :end
          AND t.stationId IN :stationIds
    """)
    BigDecimal sumLiters(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("stationIds") Collection<UUID> stationIds
    );

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.createdAt >= :start
          AND t.createdAt < :end
          AND t.stationId IN :stationIds
    """)
    BigDecimal sumRevenue(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("stationIds") Collection<UUID> stationIds
    );

    @Query("""
        SELECT t.fuelType, COALESCE(SUM(t.liters), 0), COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.createdAt >= :start
          AND t.createdAt < :end
          AND t.stationId IN :stationIds
        GROUP BY t.fuelType
    """)
    List<Object[]> getFuelSalesByType(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("stationIds") Collection<UUID> stationIds
    );

    List<TransactionSummaryResponse> findByVehicleId(UUID vehicleId);

    @Query("""
    SELECT t
    FROM Transaction t
    WHERE t.createdAt >= :from
      AND t.createdAt < :to
    ORDER BY t.createdAt DESC
    """)
    Page<Transaction> findByDateRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    Page<Transaction> findByUserId(UUID userId, Pageable pageable);

    @Query("""
        SELECT new com.example.smart_fuel_management_system.dto.station.TransactionStationResponse(
            t.stationId,
            COUNT(t.id),
            COUNT(DISTINCT t.vehicleId)
        )
        FROM Transaction t
        WHERE t.stationId IN :stationIds
        GROUP BY t.stationId
        """)
    List<TransactionStationResponse> getStationTransactionStatistics(
            @Param("stationIds") List<UUID> stationIds
    );

    List<Transaction> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);
}
