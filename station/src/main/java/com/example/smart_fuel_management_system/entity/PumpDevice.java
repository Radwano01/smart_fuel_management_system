package com.example.smart_fuel_management_system.entity;

import com.example.smart_fuel_management_system.enums.DeviceStatusType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "pump_devices",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pump_device_device_id",
                        columnNames = "device_id"
                ),
                @UniqueConstraint(
                        name = "uk_pump_device_pump_id",
                        columnNames = "pump_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PumpDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "device_id",
            nullable = false,
            unique = true,
            updatable = false
    )
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceStatusType status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "pump_id",
            unique = true,
            foreignKey = @ForeignKey(
                    name = "fk_pump_device_pump"
            )
    )
    private Pump pump;
}