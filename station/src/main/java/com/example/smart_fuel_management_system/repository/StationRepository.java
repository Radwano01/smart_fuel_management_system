package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.dto.station.StationTransactionResponse;
import com.example.smart_fuel_management_system.entity.Station;
import com.example.smart_fuel_management_system.enums.StationStatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StationRepository extends JpaRepository<Station, UUID> {
    long countByStatus(StationStatusType stationStatusType);
    @Query("""
        SELECT s
        FROM Station s
        WHERE
            (
                :search IS NULL
                OR TRIM(:search) = ''
                OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(s.city) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(s.address) LIKE LOWER(CONCAT('%', :search, '%'))
            )
        AND (:status IS NULL OR s.status = :status)
        """)
    Page<Station> searchAndFilter(
            @Param("search") String search,
            @Param("status") StationStatusType status,
            Pageable pageable
    );
    @Query("""
        SELECT new com.example.smart_fuel_management_system.dto.StationTransactionResponse(
            s.id,
            s.name,
            s.city,
            s.address
        )
        FROM Station s
        WHERE s.id IN :ids
        """)
    List<StationTransactionResponse> findStationsByIds(
            @Param("ids") List<UUID> ids
    );
}