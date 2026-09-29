package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.dto.FuelTypeSalesResponse;
import com.example.smart_fuel_management_system.dto.TransactionStationResponse;
import com.example.smart_fuel_management_system.dto.TransactionSummaryResponse;
import com.example.smart_fuel_management_system.entity.Transaction;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.enums.PaymentStatusType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EntityManager entityManager;

    private static String createdAtColumn;

    private UUID userId;
    private UUID anotherUserId;
    private UUID stationId;
    private UUID anotherStationId;
    private UUID vehicleId;
    private UUID anotherVehicleId;
    private UUID pumpId;
    private UUID anotherPumpId;
    private UUID paymentId;
    private UUID fuelSessionId;
    private LocalDateTime baseTime;

    private UUID firstId;
    private UUID secondId;
    private UUID thirdId;

    @BeforeEach
    void setUp() {
        userId           = UUID.randomUUID();
        anotherUserId    = UUID.randomUUID();
        stationId        = UUID.randomUUID();
        anotherStationId = UUID.randomUUID();
        vehicleId        = UUID.randomUUID();
        anotherVehicleId = UUID.randomUUID();
        pumpId           = UUID.randomUUID();
        anotherPumpId    = UUID.randomUUID();
        paymentId        = UUID.randomUUID();
        fuelSessionId    = UUID.randomUUID();
        baseTime         = LocalDateTime.of(2026, 9, 10, 10, 0);

        Transaction t1 = transactionRepository.save(createTransaction(
                userId, stationId, vehicleId, pumpId, paymentId, fuelSessionId,
                FuelType.GASOLINE, "10.00", "50.00", "500.00", baseTime));
        firstId = t1.getId();

        Transaction t2 = transactionRepository.save(createTransaction(
                userId, stationId, anotherVehicleId, anotherPumpId,
                UUID.randomUUID(), UUID.randomUUID(),
                FuelType.DIESEL, "20.00", "45.00", "900.00", baseTime.plusHours(1)));
        secondId = t2.getId();

        Transaction t3 = transactionRepository.save(createTransaction(
                anotherUserId, anotherStationId, anotherVehicleId, UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(),
                FuelType.GASOLINE, "5.00", "50.00", "250.00", baseTime.plusHours(2)));
        thirdId = t3.getId();

        entityManager.flush();
        forceCreatedAt(firstId, baseTime);
        forceCreatedAt(secondId, baseTime.plusHours(1));
        forceCreatedAt(thirdId, baseTime.plusHours(2));
        entityManager.clear();
    }

    @Nested
    @DisplayName("existsByPaymentId")
    class ExistsByPaymentId {

        @Test
        void returnsTrueWhenPaymentExists() {
            // given
            UUID searchId = paymentId;

            // when
            boolean exists = transactionRepository.existsByPaymentId(searchId);

            // then
            assertThat(exists).isTrue();
        }

        @Test
        void returnsFalseWhenPaymentDoesNotExist() {
            // given
            UUID searchId = UUID.randomUUID();

            // when
            boolean exists = transactionRepository.existsByPaymentId(searchId);

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("findByUserId / countByUserId / findByIdAndUserId")
    class UserScoped {

        @Test
        void findByUserId_returnsOnlyThatUsersRows() {
            // given
            UUID searchUserId = userId;

            // when
            List<Transaction> result = transactionRepository.findByUserId(searchUserId);

            // then
            assertThat(result)
                    .hasSize(2)
                    .allMatch(t -> t.getUserId().equals(searchUserId));
        }

        @Test
        void findByUserId_emptyForUnknownUser() {
            // given
            UUID unknownUserId = UUID.randomUUID();

            // when
            List<Transaction> result = transactionRepository.findByUserId(unknownUserId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void countByUserId_countsOnlyThatUsersRows() {
            // given
            UUID searchUserId = userId;

            // when
            long count = transactionRepository.countByUserId(searchUserId);

            // then
            assertThat(count).isEqualTo(2);
        }

        @Test
        void countByUserId_zeroForUnknownUser() {
            // given
            UUID unknownUserId = UUID.randomUUID();

            // when
            long count = transactionRepository.countByUserId(unknownUserId);

            // then
            assertThat(count).isZero();
        }

        @Test
        void findByIdAndUserId_matchesOwner() {
            // given
            UUID searchId = firstId;
            UUID owner = userId;

            // when
            Optional<Transaction> result =
                    transactionRepository.findByIdAndUserId(searchId, owner);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(searchId);
            assertThat(result.get().getUserId()).isEqualTo(owner);
        }

        @Test
        void findByIdAndUserId_rejectsNonOwner() {
            // given
            UUID searchId = firstId;
            UUID wrongOwner = anotherUserId;

            // when
            Optional<Transaction> result =
                    transactionRepository.findByIdAndUserId(searchId, wrongOwner);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void findTop10ByUserIdOrderByCreatedAtDesc_newestFirstForUser() {
            // given
            UUID searchUserId = userId;

            // when
            List<Transaction> result =
                    transactionRepository.findTop10ByUserIdOrderByCreatedAtDesc(searchUserId);

            // then
            assertThat(result)
                    .extracting(Transaction::getCreatedAt)
                    .containsExactly(baseTime.plusHours(1), baseTime);
        }

        @Test
        void findTop10ByUserIdOrderByCreatedAtDesc_emptyForUnknownUser() {
            // given
            UUID unknownUserId = UUID.randomUUID();

            // when
            List<Transaction> result =
                    transactionRepository.findTop10ByUserIdOrderByCreatedAtDesc(unknownUserId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void findByUserIdWithPageable_paginatesForUser() {
            // given
            UUID searchUserId = userId;
            PageRequest pageable =
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

            // when
            Page<Transaction> page =
                    transactionRepository.findByUserId(searchUserId, pageable);

            // then
            assertThat(page.getTotalElements()).isEqualTo(2);
            assertThat(page.getContent())
                    .extracting(Transaction::getCreatedAt)
                    .containsExactly(baseTime.plusHours(1), baseTime);
        }
    }

    @Nested
    @DisplayName("Station counters")
    class StationCounters {

        @Test
        void countByStationId_countsRowsForStation() {
            // given
            UUID searchStationId = stationId;

            // when
            long count = transactionRepository.countByStationId(searchStationId);

            // then
            assertThat(count).isEqualTo(2);
        }

        @Test
        void countByStationId_zeroForUnknownStation() {
            // given
            UUID unknownStationId = UUID.randomUUID();

            // when
            long count = transactionRepository.countByStationId(unknownStationId);

            // then
            assertThat(count).isZero();
        }

        @Test
        void countVehiclesByStationId_distinctVehiclesAtStation() {
            // given
            UUID searchStationId = stationId;

            // when
            long count = transactionRepository.countVehiclesByStationId(searchStationId);

            // then
            assertThat(count).isEqualTo(2);
        }

        @Test
        void countVehiclesByStationId_zeroForUnknownStation() {
            // given
            UUID unknownStationId = UUID.randomUUID();

            // when
            long count = transactionRepository.countVehiclesByStationId(unknownStationId);

            // then
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("Global date-range aggregations")
    class GlobalDateRange {

        @Test
        void countBetween_isInclusiveOfStart_exclusiveOfEnd() {
            // given
            LocalDateTime start = baseTime;
            LocalDateTime end = baseTime.plusHours(2);

            // when
            long count = transactionRepository
                    .countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end);

            // then
            assertThat(count).isEqualTo(2);
        }

        @Test
        void countBetween_endIsExclusive() {
            // given
            LocalDateTime start = baseTime;
            LocalDateTime end = baseTime.plusHours(1);

            // when
            long count = transactionRepository
                    .countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end);

            // then
            assertThat(count).isEqualTo(1);
        }

        @Test
        void countBetween_zeroForEmptyWindow() {
            // given
            LocalDateTime start = baseTime.plusDays(1);
            LocalDateTime end = baseTime.plusDays(2);

            // when
            long count = transactionRepository
                    .countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end);

            // then
            assertThat(count).isZero();
        }

        @Test
        void sumLitersBetween_sumsMatchingRows() {
            // given
            LocalDateTime start = baseTime;
            LocalDateTime end = baseTime.plusHours(2);

            // when
            BigDecimal totalLiters = transactionRepository.sumLitersBetween(start, end);

            // then
            assertThat(totalLiters).isEqualByComparingTo("30.00");
        }

        @Test
        void sumLitersBetween_zeroWhenNoRows() {
            // given
            LocalDateTime start = baseTime.plusDays(1);
            LocalDateTime end = baseTime.plusDays(2);

            // when
            BigDecimal totalLiters = transactionRepository.sumLitersBetween(start, end);

            // then
            assertThat(totalLiters).isEqualByComparingTo("0");
        }

        @Test
        void sumAmountBetween_sumsMatchingRows() {
            // given
            LocalDateTime start = baseTime;
            LocalDateTime end = baseTime.plusHours(2);

            // when
            BigDecimal totalAmount = transactionRepository.sumAmountBetween(start, end);

            // then
            assertThat(totalAmount).isEqualByComparingTo("1400.00");
        }

        @Test
        void sumAmountBetween_zeroWhenNoRows() {
            // given
            LocalDateTime start = baseTime.plusDays(1);
            LocalDateTime end = baseTime.plusDays(2);

            // when
            BigDecimal totalAmount = transactionRepository.sumAmountBetween(start, end);

            // then
            assertThat(totalAmount).isEqualByComparingTo("0");
        }

        @Test
        void getFuelSalesByType_groupsAndOrdersByFuelType() {
            // given
            LocalDateTime start = baseTime;
            LocalDateTime end = baseTime.plusHours(3);

            // when
            List<FuelTypeSalesResponse> result =
                    transactionRepository.getFuelSalesByType(start, end);

            // then
            assertThat(result)
                    .hasSize(2)
                    .extracting(FuelTypeSalesResponse::fuelType)
                    .containsExactly(FuelType.DIESEL, FuelType.GASOLINE);

            FuelTypeSalesResponse diesel   = result.get(0);
            FuelTypeSalesResponse gasoline = result.get(1);

            assertThat(diesel.fuelVolume()).isEqualByComparingTo("20.00");
            assertThat(diesel.revenue()).isEqualByComparingTo("900.00");
            assertThat(gasoline.fuelVolume()).isEqualByComparingTo("15.00");
            assertThat(gasoline.revenue()).isEqualByComparingTo("750.00");
        }

        @Test
        void getFuelSalesByType_emptyWhenNoRows() {
            // given
            LocalDateTime start = baseTime.plusDays(1);
            LocalDateTime end = baseTime.plusDays(2);

            // when
            List<FuelTypeSalesResponse> result =
                    transactionRepository.getFuelSalesByType(start, end);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void findTop10ByOrderByCreatedAtDesc_newestFirst() {
            // given
            // (three transactions already persisted in setUp)

            // when
            List<Transaction> result =
                    transactionRepository.findTop10ByOrderByCreatedAtDesc();

            // then
            assertThat(result)
                    .extracting(Transaction::getCreatedAt)
                    .containsExactly(
                            baseTime.plusHours(2),
                            baseTime.plusHours(1),
                            baseTime);
        }
    }

    @Nested
    @DisplayName("Station-scoped date-range aggregations")
    class StationScopedDateRange {

        @Test
        void countForStations_countsOnlyGivenStations() {
            // given
            LocalDateTime start = baseTime;
            LocalDateTime end = baseTime.plusHours(3);
            List<UUID> stationIds = List.of(stationId);

            // when
            long count = transactionRepository
                    .countByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndStationIdIn(
                            start, end, stationIds);

            // then
            assertThat(count).isEqualTo(2);
        }

        @Test
        void countForStations_zeroForUnknownStation() {
            // given
            LocalDateTime start = baseTime;
            LocalDateTime end = baseTime.plusHours(3);
            List<UUID> stationIds = List.of(UUID.randomUUID());

            // when
            long count = transactionRepository
                    .countByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndStationIdIn(
                            start, end, stationIds);

            // then
            assertThat(count).isZero();
        }

        @Test
        void sumLitersForStations_sumsOnlyGivenStations() {
            // given
            LocalDateTime start = baseTime;
            LocalDateTime end = baseTime.plusHours(3);
            List<UUID> stationIds = List.of(stationId);

            // when
            BigDecimal totalLiters =
                    transactionRepository.sumLiters(start, end, stationIds);

            // then
            assertThat(totalLiters).isEqualByComparingTo("30.00");
        }

        @Test
        void sumLitersForStations_zeroWhenNoStationsMatch() {
            // given
            LocalDateTime start = baseTime;
            LocalDateTime end = baseTime.plusHours(3);
            List<UUID> stationIds = List.of(UUID.randomUUID());

            // when
            BigDecimal totalLiters =
                    transactionRepository.sumLiters(start, end, stationIds);

            // then
            assertThat(totalLiters).isEqualByComparingTo("0");
        }

        @Test
        void sumRevenueForStations_sumsOnlyGivenStations() {
            // given
            LocalDateTime start = baseTime;
            LocalDateTime end = baseTime.plusHours(3);
            List<UUID> stationIds = List.of(stationId);

            // when
            BigDecimal totalRevenue =
                    transactionRepository.sumRevenue(start, end, stationIds);

            // then
            assertThat(totalRevenue).isEqualByComparingTo("1400.00");
        }

        @Test
        void sumRevenueForStations_zeroWhenNoStationsMatch() {
            // given
            LocalDateTime start = baseTime;
            LocalDateTime end = baseTime.plusHours(3);
            List<UUID> stationIds = List.of(UUID.randomUUID());

            // when
            BigDecimal totalRevenue =
                    transactionRepository.sumRevenue(start, end, stationIds);

            // then
            assertThat(totalRevenue).isEqualByComparingTo("0");
        }

        @Test
        void fuelSalesByTypeForStations_groupsByFuelType() {
            // given
            LocalDateTime start = baseTime;
            LocalDateTime end = baseTime.plusHours(3);
            List<UUID> stationIds = List.of(stationId);

            // when
            List<Object[]> rows =
                    transactionRepository.getFuelSalesByType(start, end, stationIds);

            // then
            assertThat(rows).hasSize(2);

            Object[] gasoline = rows.stream()
                    .filter(r -> FuelType.GASOLINE.equals(r[0]))
                    .findFirst().orElseThrow();
            Object[] diesel = rows.stream()
                    .filter(r -> FuelType.DIESEL.equals(r[0]))
                    .findFirst().orElseThrow();

            assertThat((BigDecimal) gasoline[1]).isEqualByComparingTo("10.00");
            assertThat((BigDecimal) gasoline[2]).isEqualByComparingTo("500.00");
            assertThat((BigDecimal) diesel[1]).isEqualByComparingTo("20.00");
            assertThat((BigDecimal) diesel[2]).isEqualByComparingTo("900.00");
        }

        @Test
        void fuelSalesByTypeForStations_emptyWhenNoStationsMatch() {
            // given
            LocalDateTime start = baseTime;
            LocalDateTime end = baseTime.plusHours(3);
            List<UUID> stationIds = List.of(UUID.randomUUID());

            // when
            List<Object[]> rows =
                    transactionRepository.getFuelSalesByType(start, end, stationIds);

            // then
            assertThat(rows).isEmpty();
        }

        @Test
        void stationTransactionStatistics_returnsPerStationCounts() {
            // given
            List<UUID> stationIds = List.of(stationId, anotherStationId);

            // when
            List<TransactionStationResponse> result =
                    transactionRepository.getStationTransactionStatistics(stationIds);

            // then
            assertThat(result).hasSize(2);

            TransactionStationResponse one = result.stream()
                    .filter(r -> r.stationId().equals(stationId))
                    .findFirst().orElseThrow();
            TransactionStationResponse two = result.stream()
                    .filter(r -> r.stationId().equals(anotherStationId))
                    .findFirst().orElseThrow();

            assertThat(one.transactionsCount()).isEqualTo(2L);
            assertThat(one.vehiclesCount()).isEqualTo(2L);
            assertThat(two.transactionsCount()).isEqualTo(1L);
            assertThat(two.vehiclesCount()).isEqualTo(1L);
        }

        @Test
        void stationTransactionStatistics_emptyForEmptyInput() {
            // given
            List<UUID> stationIds = Collections.emptyList();

            // when
            List<TransactionStationResponse> result =
                    transactionRepository.getStationTransactionStatistics(stationIds);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Pagination and ordering")
    class PaginationAndOrdering {

        @Test
        void findAllByOrderByCreatedAtDesc_returnsNewestFirst() {
            // given
            PageRequest pageable = PageRequest.of(0, 10);

            // when
            Page<Transaction> page =
                    transactionRepository.findAllByOrderByCreatedAtDesc(pageable);

            // then
            assertThat(page.getContent())
                    .extracting(Transaction::getCreatedAt)
                    .containsExactly(
                            baseTime.plusHours(2),
                            baseTime.plusHours(1),
                            baseTime);
        }

        @Test
        void findByDateRange_returnsRangeOrderedDesc() {
            // given
            LocalDateTime from = baseTime.plusMinutes(30);
            LocalDateTime to = baseTime.plusHours(3);
            PageRequest pageable =
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

            // when
            Page<Transaction> page =
                    transactionRepository.findByDateRange(from, to, pageable);

            // then
            assertThat(page.getContent())
                    .extracting(Transaction::getCreatedAt)
                    .containsExactly(baseTime.plusHours(2), baseTime.plusHours(1));
        }

        @Test
        void findByDateRange_emptyWhenNoOverlap() {
            // given
            LocalDateTime from = baseTime.plusDays(5);
            LocalDateTime to = baseTime.plusDays(6);
            PageRequest pageable =
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

            // when
            Page<Transaction> page =
                    transactionRepository.findByDateRange(from, to, pageable);

            // then
            assertThat(page.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByVehicleId")
    class ByVehicleId {

        @Test
        void returnsRowsForVehicle() {
            // given
            UUID searchVehicleId = vehicleId;

            // when
            List<TransactionSummaryResponse> result =
                    transactionRepository.findByVehicleId(searchVehicleId);

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        void emptyForUnknownVehicle() {
            // given
            UUID unknownVehicleId = UUID.randomUUID();

            // when
            List<TransactionSummaryResponse> result =
                    transactionRepository.findByVehicleId(unknownVehicleId);

            // then
            assertThat(result).isEmpty();
        }
    }

    private Transaction createTransaction(
            UUID userId,
            UUID stationId,
            UUID vehicleId,
            UUID pumpId,
            UUID paymentId,
            UUID fuelSessionId,
            FuelType fuelType,
            String liters,
            String pricePerLiter,
            String amount,
            LocalDateTime createdAt) {

        return Transaction.builder()
                .paymentId(paymentId)
                .userId(userId)
                .vehicleId(vehicleId)
                .fuelSessionId(fuelSessionId)
                .stationId(stationId)
                .pumpId(pumpId)
                .fuelType(fuelType)
                .liters(new BigDecimal(liters))
                .pricePerLiter(new BigDecimal(pricePerLiter))
                .amount(new BigDecimal(amount))
                .currency("TRY")
                .status(PaymentStatusType.SUCCESS)
                .createdAt(createdAt)
                .build();
    }

    private void forceCreatedAt(UUID id, LocalDateTime ts) {
        entityManager.createNativeQuery(
                        "UPDATE " + tableName() + " SET " + createdAtColumn() + " = :ts WHERE id = :id")
                .setParameter("ts", ts)
                .setParameter("id", id)
                .executeUpdate();
    }

    private static String tableName() {
        return "transactions";
    }

    @SuppressWarnings("unchecked")
    private String createdAtColumn() {
        if (createdAtColumn == null) {
            List<Object> rows = entityManager.createNativeQuery(
                            "SELECT column_name FROM information_schema.columns " +
                                    "WHERE LOWER(table_name) = 'transactions' " +
                                    "  AND LOWER(column_name) IN ('created_at', 'createdat')")
                    .getResultList();
            createdAtColumn = rows.isEmpty()
                    ? "created_at"
                    : rows.get(0).toString();
        }
        return createdAtColumn;
    }
}