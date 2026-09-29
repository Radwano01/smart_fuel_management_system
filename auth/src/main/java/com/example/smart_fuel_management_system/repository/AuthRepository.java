package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.entity.Auth;
import com.example.smart_fuel_management_system.enums.AccountStatusType;
import com.example.smart_fuel_management_system.enums.RoleType;
import com.netflix.spectator.api.TagList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthRepository extends JpaRepository<Auth, UUID> {

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<Auth> findByEmail(String identifier);

    Optional<Auth> findByPhoneNumber(String identifier);

    @Query("SELECT a.id FROM Auth a WHERE a.email = :email")
    UUID findIdByEmail(@Param("email") String email);

    Page<Auth> findByAccountStatusTypeAndRole(
            AccountStatusType status,
            RoleType role,
            Pageable pageable
    );

    Page<Auth> findAllByRole(
            RoleType role,
            Pageable pageable
    );

    List<Auth> findAllByIdInAndRole(
            List<UUID> userIds,
            RoleType role
    );

    @Query("""
        SELECT a
        FROM Auth a
        WHERE a.role = :role
          AND LOWER(a.email) LIKE LOWER(CONCAT('%', :search, '%'))
          AND (:status IS NULL OR a.accountStatusType = :status)
    """)
    Page<Auth> searchByEmailAndRole(
            @Param("search") String search,
            @Param("role") RoleType role,
            @Param("status") AccountStatusType status,
            Pageable pageable
    );

    @Query("""
        SELECT a
        FROM Auth a
        WHERE a.id IN :userIds
          AND a.role = :role
          AND (:status IS NULL OR a.accountStatusType = :status)
    """)
    Page<Auth> searchByIdsAndRole(
            @Param("userIds") List<UUID> userIds,
            @Param("role") RoleType role,
            @Param("status") AccountStatusType status,
            Pageable pageable
    );

    @Query("""
    SELECT a
    FROM Auth a
    WHERE a.role = :role
      AND a.phoneNumber LIKE CONCAT('%', :search, '%')
      AND (:status IS NULL OR a.accountStatusType = :status)
""")
    Page<Auth> searchByPhoneNumberAndRole(
            @Param("search") String search,
            @Param("role") RoleType role,
            @Param("status") AccountStatusType status,
            Pageable pageable
    );

    Optional<Auth> findByIdAndRole(UUID employeeId, RoleType roleType);
}