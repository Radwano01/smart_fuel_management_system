package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.FuelTypeSalesResponse;
import com.example.smart_fuel_management_system.dto.PaymentEvent;
import com.example.smart_fuel_management_system.dto.StationTransactionResponse;
import com.example.smart_fuel_management_system.dto.TransactionCountResponse;
import com.example.smart_fuel_management_system.dto.TransactionDashboardSummaryResponse;
import com.example.smart_fuel_management_system.dto.TransactionDetailsResponse;
import com.example.smart_fuel_management_system.dto.TransactionResponse;
import com.example.smart_fuel_management_system.dto.TransactionStationResponse;
import com.example.smart_fuel_management_system.dto.TransactionStatisticsResponse;
import com.example.smart_fuel_management_system.dto.TransactionSummaryResponse;
import com.example.smart_fuel_management_system.dto.VehicleTransactionResponse;
import com.example.smart_fuel_management_system.entity.Transaction;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.enums.PaymentStatusType;
import com.example.smart_fuel_management_system.repository.TransactionRepository;
import com.example.smart_fuel_management_system.service.impl.client.StationClient;
import com.example.smart_fuel_management_system.service.impl.client.VehicleClient;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private VehicleClient vehicleClient;

    @Mock
    private StationClient stationClient;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private UUID userId;
    private UUID vehicleId;
    private UUID stationId;
    private UUID pumpId;
    private UUID transactionId;
    private UUID paymentId;
    private UUID fuelSessionId;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        vehicleId = UUID.randomUUID();
        stationId = UUID.randomUUID();
        pumpId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
        paymentId = UUID.randomUUID();
        fuelSessionId = UUID.randomUUID();
        baseTime = LocalDateTime.of(2026, 9, 10, 10, 0);
    }

    private Transaction buildTransaction() {
        return Transaction.builder()
                .id(transactionId)
                .paymentId(paymentId)
                .userId(userId)
                .vehicleId(vehicleId)
                .fuelSessionId(fuelSessionId)
                .stationId(stationId)
                .pumpId(pumpId)
                .fuelType(FuelType.GASOLINE)
                .liters(new BigDecimal("10.00"))
                .pricePerLiter(new BigDecimal("50.00"))
                .amount(new BigDecimal("500.00"))
                .currency("TRY")
                .status(PaymentStatusType.SUCCESS)
                .createdAt(baseTime)
                .build();
    }

    @Nested
    @DisplayName("createFromPayment")
    class CreateFromPayment {

        @Test
        void skipsWhenPaymentAlreadyExists() {
            // given
            PaymentEvent event = mock(PaymentEvent.class);
            when(event.paymentId()).thenReturn(paymentId);
            when(transactionRepository.existsByPaymentId(paymentId)).thenReturn(true);

            // when
            transactionService.createFromPayment(event);

            // then
            verify(transactionRepository, never()).save(any(Transaction.class));
        }

        @Test
        void skipsWhenStatusIsNotSuccess() {
            // given
            PaymentEvent event = mock(PaymentEvent.class);
            when(event.paymentId()).thenReturn(paymentId);
            when(event.status()).thenReturn(PaymentStatusType.FAILED);
            when(transactionRepository.existsByPaymentId(paymentId)).thenReturn(false);

            // when
            transactionService.createFromPayment(event);

            // then
            verify(transactionRepository, never()).save(any(Transaction.class));
        }

        @Test
        void savesTransactionWhenNewAndSuccessful() {
            // given
            PaymentEvent event = mock(PaymentEvent.class);
            when(event.paymentId()).thenReturn(paymentId);
            when(event.status()).thenReturn(PaymentStatusType.SUCCESS);
            when(event.userId()).thenReturn(userId);
            when(event.vehicleId()).thenReturn(vehicleId);
            when(event.fuelSessionId()).thenReturn(fuelSessionId);
            when(event.stationId()).thenReturn(stationId);
            when(event.pumpId()).thenReturn(pumpId);
            when(event.fuelType()).thenReturn(FuelType.GASOLINE);
            when(event.liters()).thenReturn(new BigDecimal("10.00"));
            when(event.pricePerLiter()).thenReturn(new BigDecimal("50.00"));
            when(event.amount()).thenReturn(new BigDecimal("500.00"));
            when(event.currency()).thenReturn("TRY");
            when(transactionRepository.existsByPaymentId(paymentId)).thenReturn(false);

            // when
            transactionService.createFromPayment(event);

            // then
            ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(captor.capture());

            Transaction saved = captor.getValue();
            assertThat(saved.getPaymentId()).isEqualTo(paymentId);
            assertThat(saved.getUserId()).isEqualTo(userId);
            assertThat(saved.getVehicleId()).isEqualTo(vehicleId);
            assertThat(saved.getFuelSessionId()).isEqualTo(fuelSessionId);
            assertThat(saved.getStationId()).isEqualTo(stationId);
            assertThat(saved.getPumpId()).isEqualTo(pumpId);
            assertThat(saved.getFuelType()).isEqualTo(FuelType.GASOLINE);
            assertThat(saved.getLiters()).isEqualByComparingTo("10.00");
            assertThat(saved.getPricePerLiter()).isEqualByComparingTo("50.00");
            assertThat(saved.getAmount()).isEqualByComparingTo("500.00");
            assertThat(saved.getCurrency()).isEqualTo("TRY");
            assertThat(saved.getStatus()).isEqualTo(PaymentStatusType.SUCCESS);
        }
    }

    @Nested
    @DisplayName("getTransaction")
    class GetTransaction {

        @Test
        void returnsFullResponseWhenVehicleAndStationExist() {
            // given
            Transaction tx = buildTransaction();
            VehicleTransactionResponse vehicle = mock(VehicleTransactionResponse.class);
            StationTransactionResponse station = mock(StationTransactionResponse.class);

            when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(tx));
            when(vehicleClient.getVehicleDetails(vehicleId)).thenReturn(Optional.of(vehicle));
            when(stationClient.getStationDetails(stationId)).thenReturn(Optional.of(station));

            // when
            TransactionResponse response = transactionService.getTransaction(transactionId);

            // then
            assertThat(response.id()).isEqualTo(transactionId);
            assertThat(response.pumpId()).isEqualTo(pumpId);
            assertThat(response.fuelType()).isEqualTo(FuelType.GASOLINE);
            assertThat(response.liters()).isEqualByComparingTo("10.00");
            assertThat(response.pricePerLiter()).isEqualByComparingTo("50.00");
            assertThat(response.amount()).isEqualByComparingTo("500.00");
            assertThat(response.createdAt()).isEqualTo(baseTime);
            assertThat(response.status()).isEqualTo(PaymentStatusType.SUCCESS);
            assertThat(response.vehicle()).isSameAs(vehicle);
            assertThat(response.station()).isSameAs(station);
        }

        @Test
        void returnsNullDetailsWhenClientsReturnEmpty() {
            // given
            Transaction tx = buildTransaction();

            when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(tx));
            when(vehicleClient.getVehicleDetails(vehicleId)).thenReturn(Optional.empty());
            when(stationClient.getStationDetails(stationId)).thenReturn(Optional.empty());

            // when
            TransactionResponse response = transactionService.getTransaction(transactionId);

            // then
            assertThat(response.vehicle()).isNull();
            assertThat(response.station()).isNull();
        }

        @Test
        void throwsEntityNotFoundWhenTransactionMissing() {
            // given
            when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> transactionService.getTransaction(transactionId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(transactionId.toString());

            verifyNoInteractions(vehicleClient, stationClient);
        }
    }

    @Nested
    @DisplayName("getTransactions")
    class GetTransactions {

        @Test
        void usesFindAllWhenNoDateRangeProvided() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Transaction> page = new PageImpl<>(List.of(buildTransaction()));

            when(transactionRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(page);
            when(vehicleClient.getVehiclesDetails(anyList())).thenReturn(List.of());
            when(stationClient.getStationsDetails(anyList())).thenReturn(List.of());

            // when
            Page<TransactionResponse> result =
                    transactionService.getTransactions(null, null, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            verify(transactionRepository).findAllByOrderByCreatedAtDesc(pageable);
            verify(transactionRepository, never()).findByDateRange(any(), any(), any());
        }

        @Test
        void usesFindByDateRangeWhenBoundsProvided() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            LocalDateTime from = baseTime.minusDays(1);
            LocalDateTime to = baseTime.plusDays(1);
            Page<Transaction> page = new PageImpl<>(List.of(buildTransaction()));

            when(transactionRepository.findByDateRange(from, to, pageable)).thenReturn(page);
            when(vehicleClient.getVehiclesDetails(anyList())).thenReturn(List.of());
            when(stationClient.getStationsDetails(anyList())).thenReturn(List.of());

            // when
            Page<TransactionResponse> result =
                    transactionService.getTransactions(from, to, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            verify(transactionRepository).findByDateRange(from, to, pageable);
            verify(transactionRepository, never()).findAllByOrderByCreatedAtDesc(any());
        }
    }

    @Nested
    @DisplayName("getByUserId")
    class GetByUserId {

        @Test
        void mapsTransactionsToSummary() {
            // given
            when(transactionRepository.findByUserId(userId))
                    .thenReturn(List.of(buildTransaction()));

            // when
            List<TransactionSummaryResponse> result = transactionService.getByUserId(userId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(transactionId);
            assertThat(result.get(0).amount()).isEqualByComparingTo("500.00");
            assertThat(result.get(0).fuelType()).isEqualTo(FuelType.GASOLINE);
            assertThat(result.get(0).createdAt()).isEqualTo(baseTime);
        }

        @Test
        void returnsEmptyWhenUserHasNoTransactions() {
            // given
            when(transactionRepository.findByUserId(userId)).thenReturn(List.of());

            // when
            List<TransactionSummaryResponse> result = transactionService.getByUserId(userId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        void returnsDetailsWithVehicleAndStation() {
            // given
            Transaction tx = buildTransaction();
            VehicleTransactionResponse vehicle = mock(VehicleTransactionResponse.class);
            StationTransactionResponse station = mock(StationTransactionResponse.class);

            when(transactionRepository.findByIdAndUserId(transactionId, userId))
                    .thenReturn(Optional.of(tx));
            when(vehicleClient.getVehicleDetails(vehicleId)).thenReturn(Optional.of(vehicle));
            when(stationClient.getStationDetails(stationId)).thenReturn(Optional.of(station));

            // when
            TransactionDetailsResponse response = transactionService.getById(transactionId, userId);

            // then
            assertThat(response.id()).isEqualTo(transactionId);
            assertThat(response.amount()).isEqualByComparingTo("500.00");
            assertThat(response.currency()).isEqualTo("TRY");
            assertThat(response.liters()).isEqualByComparingTo("10.00");
            assertThat(response.pricePerLiter()).isEqualByComparingTo("50.00");
            assertThat(response.fuelType()).isEqualTo(FuelType.GASOLINE);
            assertThat(response.status()).isEqualTo(PaymentStatusType.SUCCESS);
            assertThat(response.createdAt()).isEqualTo(baseTime);
            assertThat(response.vehicle()).isSameAs(vehicle);
            assertThat(response.station()).isSameAs(station);
        }

        @Test
        void throwsWhenTransactionNotOwnedByUser() {
            // given
            when(transactionRepository.findByIdAndUserId(transactionId, userId))
                    .thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> transactionService.getById(transactionId, userId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Transaction not found");

            verifyNoInteractions(vehicleClient, stationClient);
        }
    }

    @Nested
    @DisplayName("simple counters and history")
    class SimpleCountersAndHistory {

        @Test
        void getUserTransactionCount_wrapsRepositoryCount() {
            // given
            when(transactionRepository.countByUserId(userId)).thenReturn(7L);

            // when
            TransactionCountResponse response =
                    transactionService.getUserTransactionCount(userId);

            // then
            assertThat(response.count()).isEqualTo(7L);
        }

        @Test
        void getTransactionsCountByStationId_wrapsCounts() {
            // given
            when(transactionRepository.countByStationId(stationId)).thenReturn(4L);
            when(transactionRepository.countVehiclesByStationId(stationId)).thenReturn(3L);

            // when
            TransactionStationResponse response =
                    transactionService.getTransactionsCountByStationId(stationId);

            // then
            assertThat(response.stationId()).isEqualTo(stationId);
            assertThat(response.transactionsCount()).isEqualTo(4L);
            assertThat(response.vehiclesCount()).isEqualTo(3L);
        }

        @Test
        void getVehicleTransactionHistory_delegatesToRepository() {
            // given
            TransactionSummaryResponse summary = mock(TransactionSummaryResponse.class);
            when(transactionRepository.findByVehicleId(vehicleId))
                    .thenReturn(List.of(summary));

            // when
            List<TransactionSummaryResponse> result =
                    transactionService.getVehicleTransactionHistory(vehicleId);

            // then
            assertThat(result).containsExactly(summary);
        }
    }

    @Nested
    @DisplayName("getStationsTransactions")
    class GetStationsTransactions {

        @Test
        void returnsEmptyListWhenInputIsNull() {
            // given
            List<UUID> input = null;

            // when
            List<TransactionStationResponse> result =
                    transactionService.getStationsTransactions(input);

            // then
            assertThat(result).isEmpty();
            verifyNoInteractions(transactionRepository);
        }

        @Test
        void returnsEmptyListWhenInputIsEmpty() {
            // given
            List<UUID> input = List.of();

            // when
            List<TransactionStationResponse> result =
                    transactionService.getStationsTransactions(input);

            // then
            assertThat(result).isEmpty();
            verifyNoInteractions(transactionRepository);
        }

        @Test
        void delegatesToRepositoryWhenIdsProvided() {
            // given
            TransactionStationResponse stationResponse = mock(TransactionStationResponse.class);
            when(transactionRepository.getStationTransactionStatistics(List.of(stationId)))
                    .thenReturn(List.of(stationResponse));

            // when
            List<TransactionStationResponse> result =
                    transactionService.getStationsTransactions(List.of(stationId));

            // then
            assertThat(result).containsExactly(stationResponse);
        }
    }

    @Nested
    @DisplayName("recentTransactions")
    class RecentTransactions {

        @Test
        void mapsTop10ToSummary() {
            // given
            when(transactionRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId))
                    .thenReturn(List.of(buildTransaction()));

            // when
            List<TransactionSummaryResponse> result =
                    transactionService.recentTransactions(userId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(transactionId);
            assertThat(result.get(0).amount()).isEqualByComparingTo("500.00");
            assertThat(result.get(0).fuelType()).isEqualTo(FuelType.GASOLINE);
            assertThat(result.get(0).createdAt()).isEqualTo(baseTime);
        }

        @Test
        void returnsEmptyWhenUserHasNoTransactions() {
            // given
            when(transactionRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId))
                    .thenReturn(List.of());

            // when
            List<TransactionSummaryResponse> result =
                    transactionService.recentTransactions(userId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUserTransactions")
    class GetUserTransactions {

        @Test
        void returnsEmptyPageWhenUserHasNoTransactions() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Transaction> empty = new PageImpl<>(List.of(), pageable, 0);

            when(transactionRepository.findByUserId(userId, pageable)).thenReturn(empty);
            when(vehicleClient.getVehiclesDetails(List.of())).thenReturn(List.of());
            when(stationClient.getStationsDetails(List.of())).thenReturn(List.of());

            // when
            Page<TransactionResponse> result =
                    transactionService.getUserTransactions(userId, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        void enrichesEachTransactionWithVehicleAndStation() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Transaction> page = new PageImpl<>(List.of(buildTransaction()), pageable, 1);

            VehicleTransactionResponse vehicle = mock(VehicleTransactionResponse.class);
            when(vehicle.id()).thenReturn(vehicleId);

            StationTransactionResponse station = mock(StationTransactionResponse.class);
            when(station.id()).thenReturn(stationId);

            when(transactionRepository.findByUserId(userId, pageable)).thenReturn(page);
            when(vehicleClient.getVehiclesDetails(List.of(vehicleId))).thenReturn(List.of(vehicle));
            when(stationClient.getStationsDetails(List.of(stationId))).thenReturn(List.of(station));

            // when
            Page<TransactionResponse> result =
                    transactionService.getUserTransactions(userId, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            TransactionResponse response = result.getContent().get(0);
            assertThat(response.id()).isEqualTo(transactionId);
            assertThat(response.vehicle()).isSameAs(vehicle);
            assertThat(response.station()).isSameAs(station);
        }
    }

    @Nested
    @DisplayName("getDashboardSummary")
    class GetDashboardSummary {

        @Test
        void aggregatesAllRepositoryCalls() {
            // given
            when(transactionRepository.count()).thenReturn(100L);
            when(transactionRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                    any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(5L);
            when(transactionRepository.sumLitersBetween(
                    any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(new BigDecimal("50.00"));
            when(transactionRepository.sumAmountBetween(
                    any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(new BigDecimal("2500.00"));
            when(transactionRepository.getFuelSalesByType(
                    any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of());
            when(transactionRepository.findTop10ByOrderByCreatedAtDesc())
                    .thenReturn(List.of());

            // when
            TransactionDashboardSummaryResponse response =
                    transactionService.getDashboardSummary();

            // then
            assertThat(response.transactionsCount()).isEqualTo(100L);
            assertThat(response.todayTransactions()).isEqualTo(5L);
            assertThat(response.todayFuelVolume()).isEqualByComparingTo("50.00");
            assertThat(response.todayRevenue()).isEqualByComparingTo("2500.00");
            assertThat(response.fuelSalesByType()).isEmpty();
            assertThat(response.recentTransactions()).isEmpty();

            verify(transactionRepository).count();
            verify(transactionRepository)
                    .countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                            any(LocalDateTime.class), any(LocalDateTime.class));
            verify(transactionRepository).sumLitersBetween(
                    any(LocalDateTime.class), any(LocalDateTime.class));
            verify(transactionRepository).sumAmountBetween(
                    any(LocalDateTime.class), any(LocalDateTime.class));
            verify(transactionRepository).getFuelSalesByType(
                    any(LocalDateTime.class), any(LocalDateTime.class));
            verify(transactionRepository).findTop10ByOrderByCreatedAtDesc();
        }
    }

    @Nested
    @DisplayName("getStatistics")
    class GetStatistics {

        @Test
        void aggregatesStatisticsOverStationIds() {
            // given
            LocalDate from = LocalDate.of(2026, 9, 1);
            LocalDate to = LocalDate.of(2026, 9, 10);
            List<UUID> stationIds = List.of(stationId);

            Object[] gasolineRow = new Object[]{
                    FuelType.GASOLINE, new BigDecimal("10.00"), new BigDecimal("500.00")
            };
            Object[] dieselRow = new Object[]{
                    FuelType.DIESEL, new BigDecimal("20.00"), new BigDecimal("900.00")
            };

            when(transactionRepository
                    .countByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndStationIdIn(
                            any(LocalDateTime.class), any(LocalDateTime.class), any()))
                    .thenReturn(3L);
            when(transactionRepository.sumLiters(
                    any(LocalDateTime.class), any(LocalDateTime.class), any()))
                    .thenReturn(new BigDecimal("30.00"));
            when(transactionRepository.sumRevenue(
                    any(LocalDateTime.class), any(LocalDateTime.class), any()))
                    .thenReturn(new BigDecimal("1400.00"));
            when(transactionRepository.getFuelSalesByType(
                    any(LocalDateTime.class), any(LocalDateTime.class), any()))
                    .thenReturn(List.of(gasolineRow, dieselRow));

            // when
            TransactionStatisticsResponse response =
                    transactionService.getStatistics(from, to, stationIds);

            // then
            assertThat(response.transactionCount()).isEqualTo(3L);
            assertThat(response.fuelVolume()).isEqualByComparingTo("30.00");
            assertThat(response.revenue()).isEqualByComparingTo("1400.00");

            List<FuelTypeSalesResponse> sales = response.fuelSalesByType();
            assertThat(sales).hasSize(2);
            assertThat(sales.get(0).fuelType()).isEqualTo(FuelType.GASOLINE);
            assertThat(sales.get(0).fuelVolume()).isEqualByComparingTo("10.00");
            assertThat(sales.get(0).revenue()).isEqualByComparingTo("500.00");
            assertThat(sales.get(1).fuelType()).isEqualTo(FuelType.DIESEL);
            assertThat(sales.get(1).fuelVolume()).isEqualByComparingTo("20.00");
            assertThat(sales.get(1).revenue()).isEqualByComparingTo("900.00");
        }
    }
}