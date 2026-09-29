package com.example.smart_fuel_management_system.config;

import com.example.smart_fuel_management_system.service.impl.VehicleSpecLoaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final VehicleSpecLoaderService loaderService;

    @Override
    public void run(String... args) throws Exception {
        loaderService.loadAll();
    }
}