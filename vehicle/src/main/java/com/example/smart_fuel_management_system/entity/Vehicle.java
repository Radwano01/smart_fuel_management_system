package com.example.smart_fuel_management_system.entity;


import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.enums.VehicleStatusType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String plateNumber;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(name = "vehicle_year", nullable = false)
    @Min(1900)
    @Max(2026)
    private int year;

    @Enumerated(EnumType.STRING)
    private FuelType fuelType;

    @Column(nullable = false)
    @Positive
    private BigDecimal tankCapacity;

    @Column(unique = true)
    private String rfidTag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatusType status;

    @Column(nullable = false)
    private UUID userId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    public Vehicle(String plateNumber, String brand,
                   String model,int year, BigDecimal tankCapacity,
                   FuelType fuelType, String rfidTag,
                   VehicleStatusType status,
                   UUID userId) {
        this.plateNumber = plateNumber;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.tankCapacity = tankCapacity;
        this.fuelType = fuelType;
        this.rfidTag = rfidTag;
        this.status = status;
        this.userId = userId;
    }
}
