package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.entity.VehicleSpec;
import com.example.smart_fuel_management_system.repository.VehicleSpecRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleSpecLoaderService {

    private final VehicleSpecRepository vehicleSpecRepository;
    private final ObjectMapper objectMapper;

    public void loadAll() throws IOException {

        if (vehicleSpecRepository.count() > 0) {
            return; // prevent duplicates
        }

        loadFile("data/gasoline-cars.json");
        loadFile("data/diesel-cars.json");
        loadFile("data/hybrid-cars.json");
        loadFile("data/electric-cars.json");
    }

    private void loadFile(String path) throws IOException {

        InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream(path);

        if (is == null) {
            throw new RuntimeException("File not found: " + path);
        }

        List<VehicleSpec> specs = objectMapper.readValue(
                is,
                new TypeReference<List<VehicleSpec>>() {}
        );

        vehicleSpecRepository.saveAll(specs);
    }
}