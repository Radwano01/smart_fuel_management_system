package com.example.smart_fuel_management_system.service;

import com.example.smart_fuel_management_system.entity.PendingIdentifierChange;

import java.util.UUID;

public interface PendingIdentifierChangeService {
    void save(PendingIdentifierChange change);
    PendingIdentifierChange findById(UUID id);
    void delete(UUID id);
    boolean existsByAuthId(UUID authId);
}