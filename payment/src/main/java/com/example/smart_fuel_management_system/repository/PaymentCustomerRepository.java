package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.entity.PaymentCustomer;
import com.example.smart_fuel_management_system.enums.PaymentProvider;
import com.example.smart_fuel_management_system.repository.projection.PreAuthProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentCustomerRepository extends JpaRepository<PaymentCustomer, UUID> {

    @Query("""
    SELECT
        pc.providerCustomerId AS customerId,
        pm.providerPaymentMethodId AS paymentMethodId
    FROM PaymentCustomer pc
    JOIN PaymentMethod pm
        ON pc.userId = pm.userId
       AND pc.provider = pm.provider
    WHERE pc.userId = :userId
      AND pc.provider = :provider
      AND pm.isDefault = true
    """)
    Optional<PreAuthProjection> findPreAuthData(
            @Param("userId") UUID userId,
            @Param("provider") PaymentProvider provider
    );

    Optional<PaymentCustomer> findByUserIdAndProvider(
            UUID userId,
            PaymentProvider provider
    );
}