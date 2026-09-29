package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.entity.OutboxEvent;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutBoxRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query("SELECT o from OutboxEvent o where o.published = false ORDER BY o.createdAt ASC")
    List<OutboxEvent> findByPublishedFalse(PageRequest pageRequest);
}
