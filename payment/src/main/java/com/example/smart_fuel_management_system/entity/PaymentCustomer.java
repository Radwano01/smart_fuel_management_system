package com.example.smart_fuel_management_system.entity;

import com.example.smart_fuel_management_system.enums.PaymentProvider;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_customer")
public class PaymentCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    private PaymentProvider provider;

    @Column(nullable = false, unique = true)
    private String providerCustomerId;
}