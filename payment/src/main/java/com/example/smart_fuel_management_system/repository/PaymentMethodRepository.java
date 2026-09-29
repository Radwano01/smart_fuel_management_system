package com.example.smart_fuel_management_system.repository;


import com.example.smart_fuel_management_system.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {

    List<PaymentMethod> getByUserId(@Param("userId") UUID userId);

    @Modifying(clearAutomatically = true)
    @Query("""
    UPDATE PaymentMethod p
    SET p.isDefault = false
    WHERE p.userId = :userId
    """)
    void clearDefaultForUser(UUID userId);

    Optional<PaymentMethod> findByIdAndUserId(UUID paymentMethodId, UUID userId);

    boolean existsDefaultByUserId(UUID userId);

    boolean existsByUserIdAndCardFingerPrint(UUID userId, String fingerPrint);

    void deleteByIdAndUserId(UUID id, UUID userId);

    @Query("""
    select pm.providerPaymentMethodId
    from PaymentMethod pm
    where pm.id = :id
      and pm.userId = :userId
    """)
    Optional<String> findStripePaymentMethodIdByIdAndUserId(UUID id, UUID userId);
}
