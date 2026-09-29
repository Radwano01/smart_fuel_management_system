package com.example.smart_fuel_management_system.entity;

import com.example.smart_fuel_management_system.enums.PaymentProvider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payment_method")
@Entity
public class PaymentMethod {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    private PaymentProvider provider;

    @Column(nullable = false, unique = true)
    private String providerPaymentMethodId;

    @Column(nullable = false)
    private String cardFingerPrint;

    private String brand;
    private String last4;
    private Integer expMonth;
    private Integer expYear;

    private boolean isDefault;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}