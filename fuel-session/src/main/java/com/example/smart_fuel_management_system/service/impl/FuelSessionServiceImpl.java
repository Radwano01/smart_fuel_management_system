package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.config.FuelProperties;
import com.example.smart_fuel_management_system.dto.*;
import com.example.smart_fuel_management_system.entity.FuelSession;
import com.example.smart_fuel_management_system.enums.FuelStatusType;
import com.example.smart_fuel_management_system.enums.PaymentStatusType;
import com.example.smart_fuel_management_system.enums.PumpStatusType;
import com.example.smart_fuel_management_system.enums.VehicleStatusType;
import com.example.smart_fuel_management_system.repository.FuelSessionRepository;
import com.example.smart_fuel_management_system.service.FuelSessionService;
import com.example.smart_fuel_management_system.service.impl.client.PaymentClient;
import com.example.smart_fuel_management_system.service.impl.client.StationClient;
import com.example.smart_fuel_management_system.service.impl.client.VehicleClient;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FuelSessionServiceImpl implements FuelSessionService {

    private final FuelSessionRepository repository;
    private final VehicleClient vehicleClient;
    private final PaymentClient paymentClient;
    private final StationClient stationClient;
    private final FuelProperties fuelProperties;
    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "pump:heartbeat:";

    @Override
    @Transactional
    public FuelSessionStartResponse startSession(StartFuelSessionRequest request) {

        VehicleResponseDTO vehicle = vehicleClient.resolveVehicle(
                request.rfidTag(),
                request.plateNumber()
        ).orElse(null);

        if (vehicle == null || vehicle.vehicleStatusType() != VehicleStatusType.ACTIVE) {
            return new FuelSessionStartResponse(
                    null,
                    FuelStatusType.CANCELLED,
                    false,
                    null,
                    null
            );
        }

        StationFuelSessionResponse station = stationClient.getStationIdAndPumpId(
                request.pumpId()
        ).orElseThrow();

        BigDecimal pricePerLiter = stationClient.getPrice(
                station.stationId(),
                vehicle.fuelType()
        ).orElseThrow(() -> new IllegalStateException(
                "No fuel price is configured for " + vehicle.fuelType()
                        + " at station " + station.stationId()
        ));

        BigDecimal estimatedAmount = vehicle.tankCapacity()
                .multiply(pricePerLiter);

        BigDecimal finalAmount = getFinalAmount(estimatedAmount);

        UUID sessionId = UUID.randomUUID();

        PaymentResponse payment = paymentClient.preAuth(
                new PaymentRequest(
                        vehicle.userId(),
                        sessionId,
                        vehicle.vehicleId(),
                        station.stationId(),
                        request.pumpId(),
                        vehicle.fuelType(),
                        vehicle.tankCapacity(),
                        pricePerLiter,
                        finalAmount,
                        "TRY"
                )
        );

        if (payment == null || payment.status() != PaymentStatusType.SUCCESS) {
            throw new IllegalStateException("Pre-authorization failed");
        }

        FuelSession session = new FuelSession(
                sessionId,
                vehicle.vehicleId(),
                vehicle.userId(),
                station.stationId(),
                station.pumpId(),
                pricePerLiter,
                vehicle.fuelType(),
                payment.paymentIntentId()
        );

        session.start();

        FuelSession saved = repository.save(session);

        updatePumpStatus(request.pumpId(), PumpStatusType.FUELING);

        return new FuelSessionStartResponse(
                saved.getId(),
                saved.getStatus(),
                true,
                vehicle.fuelType(),
                finalAmount
        );


    }

    @Override
    @Transactional
    public FuelSessionDTO stopSession(UUID sessionId, StopFuelSessionDTO request) {

        FuelSession session = repository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));

        if (session.getStatus() != FuelStatusType.STARTED && session.getStatus() != FuelStatusType.PAUSED) {
            throw new IllegalStateException("Session is not active");
        }

        BigDecimal pricePerLiter = session.getPricePerLiter();

        BigDecimal liters = request.liters();

        BigDecimal totalCost = liters.multiply(pricePerLiter);

        // STEP 2: capture payment
        CaptureResponse capture = paymentClient.capture(
                new CaptureRequest(
                        session.getPaymentIntentId(),
                        totalCost,
                        liters,
                        pricePerLiter
                )
        );

        if (capture == null || capture.status() != PaymentStatusType.PROCESSING) {
            throw new IllegalStateException("Payment capture failed");
        }

        // STEP 3: complete session
        session.complete(
                liters,
                totalCost,
                capture.paymentIntentId()
        );

        FuelSession saved = repository.save(session);

        updatePumpStatus(session.getPumpId(), PumpStatusType.ONLINE);

        return mapToDTO(saved);
    }

    @Transactional
    @Override
    public void pauseSession(UUID sessionId) {

        FuelSession session = repository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));

        session.pause();

        FuelSession saved = repository.save(session);

        updatePumpStatus(session.getPumpId(), PumpStatusType.PAUSED);

        mapToDTO(saved);
    }

    @Transactional
    @Override
    public void resumeSession(UUID sessionId) {

        FuelSession session = repository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));

        session.resume();

        FuelSession saved = repository.save(session);

        updatePumpStatus(session.getPumpId(), PumpStatusType.FUELING);

        mapToDTO(saved);
    }

    private BigDecimal getFinalAmount(BigDecimal baseAmount) {
        BigDecimal buffered = baseAmount.add(
                baseAmount.multiply(fuelProperties.getPreauth().getBufferPercentage())
        );

        BigDecimal min = fuelProperties.getPreauth().getMinimumAmount();
        BigDecimal max = fuelProperties.getPreauth().getMaximumAmount();

        BigDecimal finalAmount = buffered;

        if (finalAmount.compareTo(min) < 0) {
            finalAmount = min;
        }

        if (finalAmount.compareTo(max) > 0) {
            finalAmount = max;
        }
        return finalAmount;
    }

    private FuelSessionDTO mapToDTO(FuelSession session) {
        return FuelSessionDTO.builder()
                .sessionId(session.getId())
                .vehicleId(session.getVehicleId())
                .stationId(session.getStationId())
                .paymentIntentId(session.getPaymentIntentId())
                .fuelType(session.getFuelType())
                .liters(session.getLiters())
                .totalCost(session.getTotalCost())
                .status(session.getStatus())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .build();
    }

    private void updatePumpStatus(UUID pumpId, PumpStatusType statusType){
        String key = KEY_PREFIX + pumpId;

        if (redisTemplate.hasKey(key)) {
            redisTemplate.opsForHash().put(
                    key,
                    "status",
                    statusType.name()
            );
        }
    }
}