package com.example.smart_fuel_management_system.entity;

import com.example.smart_fuel_management_system.enums.FuelType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vehicles_specs",
    uniqueConstraints = @UniqueConstraint(
            columnNames = {"brand", "model", "spec_year", "fuel_type"}
    )
)

public class VehicleSpec {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(name = "spec_year", nullable = false)
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false)
    private FuelType fuelType;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal tankCapacity;
}
