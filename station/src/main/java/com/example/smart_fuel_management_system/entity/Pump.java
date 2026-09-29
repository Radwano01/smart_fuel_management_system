package com.example.smart_fuel_management_system.entity;

import com.example.smart_fuel_management_system.enums.FuelType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "pumps", uniqueConstraints = {
        @UniqueConstraint(
                columnNames = {"station_id", "pump_number"}
        )
})
public class Pump {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private long pumpNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false)
    private Set<FuelType> fuelTypes = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @OneToOne(
            mappedBy = "pump",
            fetch = FetchType.LAZY
    )
    private PumpDevice device;

    public Pump(long pumpNumber,
                Set<FuelType> fuelTypes,
                Station station) {
        this.pumpNumber = pumpNumber;
        this.fuelTypes = fuelTypes;
        this.station = station;
    }
}