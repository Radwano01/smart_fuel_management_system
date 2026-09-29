package com.example.smart_fuel_management_system.entity;

import com.example.smart_fuel_management_system.enums.FuelType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "missing_vehicle_specs",
    uniqueConstraints = @UniqueConstraint(
            columnNames = {"brand", "model", "spec_year", "fuel_type"}
    )
)
public class MissingVehicleSpec {

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

    public MissingVehicleSpec(String brand, String model,
                              int year, FuelType fuelType) {
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.fuelType = fuelType;
    }
}