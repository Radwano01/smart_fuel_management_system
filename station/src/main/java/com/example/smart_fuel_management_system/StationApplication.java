package com.example.smart_fuel_management_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.example.smart_fuel_management_system.config.MqttProperties;

@EnableConfigurationProperties(MqttProperties.class)
@SpringBootApplication
public class StationApplication {
    public static void main(String[] args) {
        SpringApplication.run(StationApplication.class, args);
    }
}