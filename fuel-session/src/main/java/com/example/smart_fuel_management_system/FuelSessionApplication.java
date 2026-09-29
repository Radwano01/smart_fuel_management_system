package com.example.smart_fuel_management_system;


import com.example.smart_fuel_management_system.config.FuelProperties;
import com.example.smart_fuel_management_system.config.MqttProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({FuelProperties.class, MqttProperties.class})
@SpringBootApplication
public class FuelSessionApplication {
    public static void main(String[] args) {
        SpringApplication.run(FuelSessionApplication.class, args);
    }
}