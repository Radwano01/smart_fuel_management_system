package com.example.smart_fuel_management_system.entity;

import com.example.smart_fuel_management_system.enums.FuelPriceStatusType;
import com.example.smart_fuel_management_system.enums.FuelType;
import jakarta.persistence.*;
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
@Table(name = "fuel_prices")
public class FuelPrice {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FuelType fuelType;

    @Column(nullable = false)
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "station_id")
    private Station station;

    @Enumerated(EnumType.STRING)
    private FuelPriceStatusType fuelPriceStatusType;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public FuelPrice(FuelType fuelType,
                     BigDecimal price,
                     Station station,
                     FuelPriceStatusType fuelPriceStatusType) {
        this.fuelType = fuelType;
        this.price = price;
        this.station = station;
        this.fuelPriceStatusType = fuelPriceStatusType;
    }
}