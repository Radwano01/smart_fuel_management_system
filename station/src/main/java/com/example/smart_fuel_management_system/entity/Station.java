package com.example.smart_fuel_management_system.entity;

import com.example.smart_fuel_management_system.enums.StationStatusType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "stations")
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String city;
    private String address;
    private String contactInformation;
    BigDecimal latitude;
    BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StationStatusType status;

    @OneToMany(
            mappedBy = "station",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private List<FuelPrice> fuelPrices = new ArrayList<>();

    @OneToMany(
        mappedBy = "station",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    private List<Pump> pumps = new ArrayList<>();

    @OneToOne(
            mappedBy = "station",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    private StationEmployee employee;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Station(String name,
                   String city,
                   String address,
                   StationStatusType status,
                   String contactInformation,
                   BigDecimal latitude,
                   BigDecimal longitude) {
        this.name = name;
        this.city = city;
        this.address = address;
        this.status = status;
        this.contactInformation = contactInformation;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}