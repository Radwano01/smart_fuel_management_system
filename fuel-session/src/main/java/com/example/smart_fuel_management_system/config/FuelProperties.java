package com.example.smart_fuel_management_system.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@Setter
@Getter
@ConfigurationProperties(prefix = "fuel")
public class FuelProperties {

    private Preauth preauth;

    @Setter
    @Getter
    public static class Preauth {

        private BigDecimal bufferPercentage;
        private BigDecimal minimumAmount;
        private BigDecimal maximumAmount;
    }
}
