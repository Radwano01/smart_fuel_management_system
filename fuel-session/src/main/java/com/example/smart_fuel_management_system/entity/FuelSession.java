package com.example.smart_fuel_management_system.entity;

import com.example.smart_fuel_management_system.enums.FuelStatusType;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.enums.PaymentStatusType;
import jakarta.persistence.*;
import jakarta.ws.rs.BadRequestException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "fuel-sessions")
public class FuelSession {

    @Id
    private UUID id;

    private UUID userId;
    private UUID vehicleId;
    private UUID stationId;
    private UUID pumpId;

    @Enumerated(EnumType.STRING)
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    private FuelStatusType status;

    private BigDecimal pricePerLiter;
    private BigDecimal liters;
    private BigDecimal totalCost;

    private String paymentIntentId;

    private BigDecimal authorizedAmount;

    @Enumerated(EnumType.STRING)
    private PaymentStatusType paymentStatus;

    private String currency;
    private String paymentFailureReason;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    private BigDecimal currentLiters;
    private LocalDateTime lastUpdateAt;

    public FuelSession(UUID sessionId,
                       UUID vehicleId,
                       UUID userId,
                       UUID stationId,
                       UUID pumpId,
                       BigDecimal pricePerLiter,
                       FuelType fuelType,
                       String paymentIntentId) {
        this.id = sessionId;
        this.vehicleId = vehicleId;
        this.userId = userId;
        this.stationId = stationId;
        this.pumpId = pumpId;
        this.pricePerLiter = pricePerLiter;
        this.fuelType = fuelType;
        this.paymentIntentId = paymentIntentId;
        this.status = FuelStatusType.STARTED;
    }

    // ===== DOMAIN METHODS =====

    public void start() {
        if (this.status != FuelStatusType.STARTED) {
            throw new BadRequestException("Session already started or invalid state");
        }

        this.startedAt = LocalDateTime.now();
    }

    public void complete(BigDecimal liters,
                         BigDecimal cost,
                         String paymentIntentId) {

        if (this.status != FuelStatusType.STARTED && this.status != FuelStatusType.PAUSED) {
            throw new BadRequestException("Only STARTED or PAUSED sessions can be completed");
        }

        this.status = FuelStatusType.COMPLETED;
        this.liters = liters;
        this.totalCost = cost;
        this.paymentIntentId = paymentIntentId;
        this.endedAt = LocalDateTime.now();
    }

    public void pause() {
        if (this.status != FuelStatusType.STARTED) {
            throw new IllegalStateException("Only STARTED sessions can be paused");
        }
        this.status = FuelStatusType.PAUSED;
    }

    public void resume() {
        if (this.status != FuelStatusType.PAUSED) {
            throw new IllegalStateException("Only PAUSED sessions can be resumed");
        }
        this.status = FuelStatusType.STARTED;
    }

    public void cancel() {

        if (this.status == FuelStatusType.COMPLETED) {
            throw new BadRequestException("Cannot cancel completed session");
        }

        this.status = FuelStatusType.CANCELLED;
        this.endedAt = LocalDateTime.now();
    }
}
