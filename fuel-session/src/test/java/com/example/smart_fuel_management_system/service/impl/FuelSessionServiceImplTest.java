package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.config.FuelProperties;
import com.example.smart_fuel_management_system.dto.CaptureResponse;
import com.example.smart_fuel_management_system.dto.FuelSessionDTO;
import com.example.smart_fuel_management_system.dto.FuelSessionStartResponse;
import com.example.smart_fuel_management_system.dto.PaymentRequest;
import com.example.smart_fuel_management_system.dto.PaymentResponse;
import com.example.smart_fuel_management_system.dto.StationFuelSessionResponse;
import com.example.smart_fuel_management_system.dto.StartFuelSessionRequest;
import com.example.smart_fuel_management_system.dto.StopFuelSessionDTO;
import com.example.smart_fuel_management_system.dto.VehicleResponseDTO;
import com.example.smart_fuel_management_system.entity.FuelSession;
import com.example.smart_fuel_management_system.enums.FuelStatusType;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.enums.PaymentStatusType;
import com.example.smart_fuel_management_system.enums.PumpStatusType;
import com.example.smart_fuel_management_system.enums.VehicleStatusType;
import com.example.smart_fuel_management_system.repository.FuelSessionRepository;
import com.example.smart_fuel_management_system.service.impl.client.PaymentClient;
import com.example.smart_fuel_management_system.service.impl.client.StationClient;
import com.example.smart_fuel_management_system.service.impl.client.VehicleClient;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FuelSessionServiceImplTest {

    @Mock
    private FuelSessionRepository repository;

    @Mock
    private VehicleClient vehicleClient;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private StationClient stationClient;

    @Mock
    private FuelProperties fuelProperties;

    @Mock
    private FuelProperties.Preauth preauth;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private FuelSessionServiceImpl service;

    @Test
    void startSession_shouldReturnCancelled_whenVehicleIsNotFound() {
        // given
        StartFuelSessionRequest request =
                new StartFuelSessionRequest(
                        "RFID123",
                        "27ABC123",
                        UUID.randomUUID()
                );

        when(vehicleClient.resolveVehicle(
                request.rfidTag(),
                request.plateNumber()
        )).thenReturn(Optional.empty());

        // when
        FuelSessionStartResponse result =
                service.startSession(request);

        // then
        assertThat(result.status())
                .isEqualTo(FuelStatusType.CANCELLED);

        assertThat(result.allowed())
                .isFalse();

        verify(stationClient, never())
                .getStationIdAndPumpId(any());

        verify(paymentClient, never())
                .preAuth(any());
    }

    @Test
    void startSession_shouldReturnCancelled_whenVehicleIsInactive() {
        // given
        StartFuelSessionRequest request =
                new StartFuelSessionRequest(
                        "RFID123",
                        "27ABC123",
                        UUID.randomUUID()
                );

        VehicleResponseDTO vehicle =
                org.mockito.Mockito.mock(VehicleResponseDTO.class);

        when(vehicleClient.resolveVehicle(
                request.rfidTag(),
                request.plateNumber()
        )).thenReturn(Optional.of(vehicle));

        when(vehicle.vehicleStatusType())
                .thenReturn(VehicleStatusType.INACTIVE);

        // when
        FuelSessionStartResponse result =
                service.startSession(request);

        // then
        assertThat(result.status())
                .isEqualTo(FuelStatusType.CANCELLED);

        assertThat(result.allowed())
                .isFalse();

        verify(stationClient, never())
                .getStationIdAndPumpId(any());

        verify(paymentClient, never())
                .preAuth(any());
    }

    @Test
    void startSession_shouldCreateSessionAndSetPumpFueling_whenPreAuthorizationSucceeds() {
        // given
        UUID pumpId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();

        StartFuelSessionRequest request =
                new StartFuelSessionRequest(
                        "RFID123",
                        "27ABC123",
                        pumpId
                );

        VehicleResponseDTO vehicle =
                org.mockito.Mockito.mock(VehicleResponseDTO.class);

        StationFuelSessionResponse station =
                org.mockito.Mockito.mock(StationFuelSessionResponse.class);

        PaymentResponse payment =
                org.mockito.Mockito.mock(PaymentResponse.class);

        FuelSession savedSession =
                org.mockito.Mockito.mock(FuelSession.class);

        when(fuelProperties.getPreauth())
                .thenReturn(preauth);

        when(preauth.getBufferPercentage())
                .thenReturn(BigDecimal.ZERO);

        when(preauth.getMinimumAmount())
                .thenReturn(BigDecimal.ZERO);

        when(preauth.getMaximumAmount())
                .thenReturn(new BigDecimal("100000"));

        when(vehicleClient.resolveVehicle(
                request.rfidTag(),
                request.plateNumber()
        )).thenReturn(Optional.of(vehicle));

        when(vehicle.vehicleStatusType())
                .thenReturn(VehicleStatusType.ACTIVE);

        when(vehicle.vehicleId())
                .thenReturn(vehicleId);

        when(vehicle.userId())
                .thenReturn(userId);

        when(vehicle.fuelType())
                .thenReturn(FuelType.GASOLINE);

        when(vehicle.tankCapacity())
                .thenReturn(new BigDecimal("50"));

        when(stationClient.getStationIdAndPumpId(pumpId))
                .thenReturn(Optional.of(station));

        when(station.stationId())
                .thenReturn(stationId);

        when(station.pumpId())
                .thenReturn(pumpId);

        when(stationClient.getPrice(
                stationId,
                FuelType.GASOLINE
        )).thenReturn(Optional.of(new BigDecimal("40")));

        when(payment.status())
                .thenReturn(PaymentStatusType.SUCCESS);

        when(payment.paymentIntentId())
                .thenReturn("payment-intent-id");

        when(paymentClient.preAuth(any(PaymentRequest.class)))
                .thenReturn(payment);

        when(repository.save(any(FuelSession.class)))
                .thenReturn(savedSession);

        when(savedSession.getId())
                .thenReturn(UUID.randomUUID());

        when(savedSession.getStatus())
                .thenReturn(FuelStatusType.STARTED);

        when(redisTemplate.hasKey("pump:heartbeat:" + pumpId))
                .thenReturn(true);

        when(redisTemplate.opsForHash())
                .thenReturn(hashOperations);

        // when
        FuelSessionStartResponse result =
                service.startSession(request);

        // then
        assertThat(result)
                .isNotNull();

        assertThat(result.status())
                .isEqualTo(FuelStatusType.STARTED);

        assertThat(result.allowed())
                .isTrue();

        assertThat(result.fuelType())
                .isEqualTo(FuelType.GASOLINE);

        assertThat(result.authorizedAmount())
                .isEqualByComparingTo("2000");

        verify(paymentClient)
                .preAuth(any(PaymentRequest.class));

        verify(repository)
                .save(any(FuelSession.class));

        verify(hashOperations)
                .put(
                        "pump:heartbeat:" + pumpId,
                        "status",
                        PumpStatusType.FUELING.name()
                );
    }

    @Test
    void startSession_shouldThrowException_whenPreAuthorizationFails() {
        // given
        UUID pumpId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();

        StartFuelSessionRequest request =
                new StartFuelSessionRequest(
                        "RFID123",
                        "27ABC123",
                        pumpId
                );

        VehicleResponseDTO vehicle =
                org.mockito.Mockito.mock(VehicleResponseDTO.class);

        StationFuelSessionResponse station =
                org.mockito.Mockito.mock(StationFuelSessionResponse.class);

        PaymentResponse payment =
                org.mockito.Mockito.mock(PaymentResponse.class);

        when(fuelProperties.getPreauth())
                .thenReturn(preauth);

        when(preauth.getBufferPercentage())
                .thenReturn(BigDecimal.ZERO);

        when(preauth.getMinimumAmount())
                .thenReturn(BigDecimal.ZERO);

        when(preauth.getMaximumAmount())
                .thenReturn(new BigDecimal("100000"));

        when(vehicleClient.resolveVehicle(
                request.rfidTag(),
                request.plateNumber()
        )).thenReturn(Optional.of(vehicle));

        when(vehicle.vehicleStatusType())
                .thenReturn(VehicleStatusType.ACTIVE);

        when(vehicle.vehicleId())
                .thenReturn(vehicleId);

        when(vehicle.userId())
                .thenReturn(userId);

        when(vehicle.fuelType())
                .thenReturn(FuelType.GASOLINE);

        when(vehicle.tankCapacity())
                .thenReturn(new BigDecimal("50"));

        when(stationClient.getStationIdAndPumpId(pumpId))
                .thenReturn(Optional.of(station));

        when(station.stationId())
                .thenReturn(stationId);

        when(stationClient.getPrice(
                stationId,
                FuelType.GASOLINE
        )).thenReturn(Optional.of(new BigDecimal("40")));

        when(payment.status())
                .thenReturn(PaymentStatusType.FAILED);

        when(paymentClient.preAuth(any(PaymentRequest.class)))
                .thenReturn(payment);

        // when & then
        assertThatThrownBy(() ->
                service.startSession(request)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Pre-authorization failed");

        verify(repository, never())
                .save(any(FuelSession.class));
    }

    @Test
    void stopSession_shouldCompleteSessionAndSetPumpOnline_whenCaptureSucceeds() {
        // given
        UUID sessionId = UUID.randomUUID();
        UUID pumpId = UUID.randomUUID();
        UUID paymentIntentId = UUID.randomUUID();

        FuelSession session =
                org.mockito.Mockito.mock(FuelSession.class);

        StopFuelSessionDTO request =
                new StopFuelSessionDTO(
                        new BigDecimal("10")
                );

        CaptureResponse capture =
                org.mockito.Mockito.mock(CaptureResponse.class);

        FuelSession savedSession =
                org.mockito.Mockito.mock(FuelSession.class);

        when(repository.findById(sessionId))
                .thenReturn(Optional.of(session));

        when(session.getStatus())
                .thenReturn(FuelStatusType.STARTED);

        when(session.getPricePerLiter())
                .thenReturn(new BigDecimal("40"));

        when(session.getPaymentIntentId())
                .thenReturn(String.valueOf(paymentIntentId));

        when(session.getPumpId())
                .thenReturn(pumpId);

        when(capture.status())
                .thenReturn(PaymentStatusType.PROCESSING);

        when(capture.paymentIntentId())
                .thenReturn(String.valueOf(paymentIntentId));

        when(paymentClient.capture(any()))
                .thenReturn(capture);

        when(repository.save(session))
                .thenReturn(savedSession);

        when(savedSession.getId())
                .thenReturn(sessionId);

        when(savedSession.getVehicleId())
                .thenReturn(UUID.randomUUID());

        when(savedSession.getStationId())
                .thenReturn(UUID.randomUUID());

        when(savedSession.getPaymentIntentId())
                .thenReturn(String.valueOf(paymentIntentId));

        when(savedSession.getFuelType())
                .thenReturn(FuelType.GASOLINE);

        when(savedSession.getLiters())
                .thenReturn(new BigDecimal("10"));

        when(savedSession.getTotalCost())
                .thenReturn(new BigDecimal("400"));

        when(savedSession.getStatus())
                .thenReturn(FuelStatusType.COMPLETED);

        when(redisTemplate.hasKey("pump:heartbeat:" + pumpId))
                .thenReturn(true);

        when(redisTemplate.opsForHash())
                .thenReturn(hashOperations);

        // when
        FuelSessionDTO result =
                service.stopSession(
                        sessionId,
                        request
                );

        // then
        assertThat(result)
                .isNotNull();

        assertThat(result.sessionId())
                .isEqualTo(sessionId);

        assertThat(result.liters())
                .isEqualByComparingTo("10");

        assertThat(result.totalCost())
                .isEqualByComparingTo("400");

        assertThat(result.status())
                .isEqualTo(FuelStatusType.COMPLETED);

        verify(paymentClient)
                .capture(any());

        verify(repository)
                .save(session);

        verify(hashOperations)
                .put(
                        "pump:heartbeat:" + pumpId,
                        "status",
                        PumpStatusType.ONLINE.name()
                );
    }

    @Test
    void stopSession_shouldThrowException_whenSessionDoesNotExist() {
        // given
        UUID sessionId = UUID.randomUUID();

        when(repository.findById(sessionId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                service.stopSession(
                        sessionId,
                        null
                )
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Session not found");
    }

    @Test
    void stopSession_shouldThrowException_whenSessionIsNotActive() {
        // given
        UUID sessionId = UUID.randomUUID();

        FuelSession session =
                org.mockito.Mockito.mock(FuelSession.class);

        when(repository.findById(sessionId))
                .thenReturn(Optional.of(session));

        when(session.getStatus())
                .thenReturn(FuelStatusType.COMPLETED);

        // when & then
        assertThatThrownBy(() ->
                service.stopSession(
                        sessionId,
                        null
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Session is not active");

        verify(paymentClient, never())
                .capture(any());
    }

    @Test
    void stopSession_shouldThrowException_whenPaymentCaptureFails() {
        // given
        UUID sessionId = UUID.randomUUID();

        FuelSession session =
                org.mockito.Mockito.mock(FuelSession.class);

        StopFuelSessionDTO request =
                new StopFuelSessionDTO(
                        new BigDecimal("10")
                );

        CaptureResponse capture =
                org.mockito.Mockito.mock(CaptureResponse.class);

        when(repository.findById(sessionId))
                .thenReturn(Optional.of(session));

        when(session.getStatus())
                .thenReturn(FuelStatusType.STARTED);

        when(session.getPricePerLiter())
                .thenReturn(new BigDecimal("40"));

        when(session.getPaymentIntentId())
                .thenReturn(String.valueOf(UUID.randomUUID()));

        when(capture.status())
                .thenReturn(PaymentStatusType.FAILED);

        when(paymentClient.capture(any()))
                .thenReturn(capture);

        // when & then
        assertThatThrownBy(() ->
                service.stopSession(
                        sessionId,
                        request
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Payment capture failed");

        verify(repository, never())
                .save(any(FuelSession.class));
    }

    @Test
    void pauseSession_shouldSaveSessionAndSetPumpPaused() {
        // given
        UUID sessionId = UUID.randomUUID();
        UUID pumpId = UUID.randomUUID();

        FuelSession session =
                org.mockito.Mockito.mock(FuelSession.class);

        FuelSession savedSession =
                org.mockito.Mockito.mock(FuelSession.class);

        when(repository.findById(sessionId))
                .thenReturn(Optional.of(session));

        when(session.getPumpId())
                .thenReturn(pumpId);

        when(repository.save(session))
                .thenReturn(savedSession);

        when(redisTemplate.hasKey("pump:heartbeat:" + pumpId))
                .thenReturn(true);

        when(redisTemplate.opsForHash())
                .thenReturn(hashOperations);

        // when
        service.pauseSession(sessionId);

        // then
        verify(session)
                .pause();

        verify(repository)
                .save(session);

        verify(hashOperations)
                .put(
                        "pump:heartbeat:" + pumpId,
                        "status",
                        PumpStatusType.PAUSED.name()
                );
    }

    @Test
    void resumeSession_shouldSaveSessionAndSetPumpFueling() {
        // given
        UUID sessionId = UUID.randomUUID();
        UUID pumpId = UUID.randomUUID();

        FuelSession session =
                org.mockito.Mockito.mock(FuelSession.class);

        FuelSession savedSession =
                org.mockito.Mockito.mock(FuelSession.class);

        when(repository.findById(sessionId))
                .thenReturn(Optional.of(session));

        when(session.getPumpId())
                .thenReturn(pumpId);

        when(repository.save(session))
                .thenReturn(savedSession);

        when(redisTemplate.hasKey("pump:heartbeat:" + pumpId))
                .thenReturn(true);

        when(redisTemplate.opsForHash())
                .thenReturn(hashOperations);

        // when
        service.resumeSession(sessionId);

        // then
        verify(session)
                .resume();

        verify(repository)
                .save(session);

        verify(hashOperations)
                .put(
                        "pump:heartbeat:" + pumpId,
                        "status",
                        PumpStatusType.FUELING.name()
                );
    }
}